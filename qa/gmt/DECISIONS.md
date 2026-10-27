# GMT Measurement Decisions

Decisions about the GMT measurement setup for KADAI — not about KADAI itself.

Status: `accepted` | `proposed` | `superseded (by #N)`. Entries are append-only, newest at the bottom.

## Use `kadai-gmt` as the target application
- Date / related: 2026-09-30, #2259
- Status: accepted
- Context: The setup previously measured an externally hosted, slightly modified `kadai-example-spring-boot` image.
- Decision: Use the upstream `kadai-gmt` application (image `ghcr.io/kadai-io/kadai-gmt`).
- Rationale: It is still as lightweight as the example app but includes the adjustments the improved GMT setup needs, in particular the test-data generator.
- Consequences: The measured subject is maintained in this repository; the external image is no longer needed.

## Embedded LDAP instead of an external LDAP container
- Date / related: 2026-09-30, #2268, #2269
- Status: accepted
- Context: `kadai-gmt` authenticates via LDAP but had no LDAP server configured, so API calls failed.
- Decision: Enable Spring's embedded LDAP in `kadai-gmt` to make the image self-contained.
- Consequences: No extra container in the setup. The embedded server uses a test LDIF with plaintext credentials — fine for measurements, not for production.

## Generate test data at measurement time instead of using a pre-seeded database image
- Date / related: 2026-09-30, #1896, #2094
- Status: accepted
- Context: A pre-seeded database image starts with all data already resident/cached, which biases reads towards warm-cache behavior and makes the measurement unrealistic. It was also incompatible with the current schema version.
- Decision: Start from an empty PostgreSQL 17 and generate data via `POST /kadai/api/v1/gmt/tasks`.
- Consequences: More realistic measurement behavior and a schema-compatible setup; requires a generation step before the measured flow. (Rationale corrected 2026-10-13: this does not avoid warm-cache reads — the generated data is also fully cached; see "Measurement reads are served from a warm cache" below.)

## Seed via GMT `setup-commands` rather than a flow step
- Date / related: 2026-09-30, #2259
- Status: accepted
- Context: Test data must exist before the warm-up and measurement phases.
- Decision: Seed with `setup-commands` (before the measured flow), not as a flow step.
- Consequences: The seeding cost is attributed to the Boot phase and does not contaminate the standard usage scenario (the generator persists via direct `COPY`, bypassing business logic). To track the seeding cost itself, a separate seed-only scenario would be needed.

## Discover IDs dynamically in k6
- Date / related: 2026-09-30, #2259
- Status: accepted
- Context: The generator does not reproduce the fixed IDs/keys of the old sample data — it creates `TDG_*` keys (`TDG_CLI_*` classifications, `TDG_USER_*`/`TDG_GPK_*`/`TDG_TPK_*` workbaskets) and random UUIDs.
- Decision: Discover workbaskets, classifications and task IDs via the API in k6's `setup()`.
- Consequences: Scripts are independent of the concrete data set.

## Disjoint task pools for warm-up and measurement
- Date / related: 2026-09-30, #2259
- Status: accepted
- Context: The warm-up mutates tasks (edit/transfer/claim/complete), which could affect tasks read by the measurement.
- Decision: Warm-up and measurement read disjoint task slices via `TASK_POOL_OFFSET`, from a stable ascending sort.
- Consequences: Measured reads are independent of warm-up mutations; the phases do not model one continuous session.

## Image strategy: pinned for regular and certification
- Date / related: 2026-09-30, #2259
- Status: accepted
- Context: Measurements should be reproducible per commit; the Blauer Engel certification must additionally be decoupled from later changes.
- Decision: Both the regular and the certification scenarios pin a `sha-<commit>` app image (not `latest`). The regular pin is bumped automatically; the certification pin is changed manually. The certification additionally pins the postgres and k6 images.
- Consequences: Per-commit reproducibility; regular runs follow new images through the automatic bump.

## Seed count
- Date / related: 2026-09-30, #2259
- Status: accepted
- Context: 100000 tasks is the realistic task count chosen for the original Blauer Engel certification.
- Decision: Seed 100000 tasks for the standard/certification scenarios and 1000 for the quick test/debug scenario.
- Consequences: Quick runs stay fast; representative runs use the same data volume as the original certification.

## Generator idempotency
- Date / related: 2026-09-30, #2116
- Status: accepted
- Context: `POST /kadai/api/v1/gmt/tasks` is not idempotent — a second call fails on the unique external ID `ETI:000000000000`. GMT always starts a fresh database.
- Decision: Keep relying on a fresh database; make the generator re-runnable only if repeated generation against the same database is needed.
- Consequences: Repeated local runs must recreate the database (or drop the schema) first.

## Bump the regular pin via pull request, not a direct master push
- Date / related: 2026-10-02, #2280
- Status: accepted
- Context: The bump job used to push the one-line change directly to `master`, which is covered by the repository ruleset **Master protection** (PR + 1 approval) and rejected the push (`GH013`). The built-in GitHub Actions app cannot be added as a bypass actor, so a `GITHUB_TOKEN`-based workflow can never push to or merge into `master` under this ruleset.
- Decision: The bump job opens (or updates) a pull request on a deterministic `chore/bump-gmt-pin-<sha>` branch; a KADAI team member approves and merges it. This mirrors the existing post-release snapshot-version bump flow (`update_version` / `ci/commitPoms.sh`).
- Consequences: The pin lags until a team member merges the PR; eventual consistency is acceptable because the bump only fires when `kadai-gmt` changes. The ruleset stays fully intact and the change still goes through review and an audit trail. No new infrastructure (GitHub App, deploy key) is needed.

## Pin explicit container memory limits
- Date / related: 2026-10-13, #2319
- Status: accepted
- Context: Without `mem_limit`, GMT auto-assigns each container's memory from the host's available RAM, so the limits are implicit and can differ between machines or runs.
- Decision: Set explicit limits in `compose.yml`: `app` 2G, `postgres` 2G, `k6` 1G.
- Consequences: Reproducible container limits independent of host auto-assignment. `app` and `postgres` keep the ~2 GB headroom the certification runs effectively had; `k6` only needs ~100 MB and is right-sized.

## Measurement reads are served from a warm cache (corrects the rationale of "Generate test data at measurement time")
- Date / related: 2026-10-13, #2318
- Status: accepted
- Context: The earlier decision "Generate test data at measurement time instead of using a pre-seeded database image" claimed that generating the data at measurement time avoids the warm-cache bias of a pre-seeded image. The measurement data shows this is not the case. In run `a4604939-aa5a-4a87-98c8-542a4fdd3f4d`, `kadai-postgres` reports `disk_io_read_cgroup_container = 0 B/s` in Warm-up, Pause and Standard Usage (system-wide `disk_io_read_procfs_system` is also 0). The 100000 generated tasks (~1 KB per task, ~100 MB for the heap and the eight `TASK` indexes) fit into `shared_buffers` (128 MB) plus the Linux page cache. Seeding in `[BOOT]`, the 30 s pause and the warm-up keep the working set resident, and the 2 GB container memory limit is never reached, so no page is evicted. Generating the data at measurement time therefore does not produce cold reads either; it only moves the warm-up from image-build time to the boot phase.
- Decision: Document the current behaviour honestly: the scenario measures a warm-cache steady state, and ~0 B/s disk reads for `kadai-postgres` are expected, not a measurement defect. Whether we want a cold-cache / I/O-bound scenario instead is discussed in #2318.
- Consequences: The rationale "more realistic measurement behavior" in the earlier entry is corrected — it refers to schema compatibility, not to cold-cache reads. Database disk-I/O energy is not exercised by the current setup.

## Pin the JVM options: heap sizing and garbage collector
- Date / related: 2026-10-25, #2358, #2319
- Status: accepted
- Context: With the explicit `mem_limit` from "Pin explicit container memory limits" in place, `kadai-gmt` still ran without any `JAVA_OPTS`. Two things were therefore left to the JVM: the heap size, which defaults to 25 % of the container limit (~512 MB), leaving the app far below the memory it is allowed to use and shifting the workload into GC pressure; and the garbage collector, which is selected by ergonomics (G1 in JDK 21) and depends on the detected CPU count, the memory limit and the JDK build — and the base image `eclipse-temurin:21-jre-jammy` is a floating tag. Only the heap part changes behaviour, but both are invisible assumptions without an explicit flag. The previous example image ran at ~1.41 GB heap with G1.
- Decision: Set `JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"` in `compose.yml`, i.e. ~1.44 GiB heap at the 2G app limit, G1 pinned, and the JVM fails fast instead of running with an exhausted heap.
- Rationale: A percentage of the declared limit stays correct when the limit changes and is self-explanatory in a hand-written Dockerfile; the absolute values of the previous memory calculator (`-Xmx1477439K`, `-XX:MaxMetaspaceSize`, `-XX:ReservedCodeCacheSize`, `MALLOC_ARENA_MAX`) are arbitrary numbers outside the buildpack that computed them and were deliberately not copied. `ExitOnOutOfMemoryError` makes an exhausted heap fail the run visibly instead of silently degrading the measurement. G1 is pinned because it keeps the results comparable to the previous Blue Angel measurement and because it is the most common collector in production — the setup should measure what typically runs. The previous Blue Angel measurement also ran JDK 21 (`eclipse-temurin:21`), so G1 is not only the same collector strategy but the same JVM major version; the comparison still does not mean an identical runtime, since app and image build differ. `UseParallelGC` is a defensible alternative (lower CPU cost per collection, fewer adaptive heuristics, and therefore possibly more reproducible), but it has not been measured here and would break comparability with the existing runs; it would be a separate experiment.
- Consequences: The app has roughly 2.8x more heap than before, so measurements taken before this change are not comparable with measurements taken after it. The GC pin alone changes nothing today, since G1 was already selected ergonomically. Other ergonomics remain implicit on purpose: no pause goal, no region size, no `ActiveProcessorCount`, since each additional flag would need its own justification. Everything takes effect only once a new `kadai-gmt` image is built and the pinned `sha-<commit>` tag is bumped.

## Ship no test tooling or javadocs in the measured image
- Date / related: 2026-10-25, #2358, #2359
- Status: accepted
- Context: The measured subject is the container image, so its content is a measurement concern, not only build hygiene. `kadai-test-api` is needed at runtime because `TaskTestDataController` seeds the data set through `TaskTestDataGenerator`, but the module declares its tooling in compile scope. That put JUnit, Mockito, Testcontainers (with docker-java and JNA) and the DB2 driver on the production classpath — roughly a third of the image, never executed, and a Docker client inside the measured container. In addition, the `copy-kadai-core-docs` / `copy-kadai-spring-docs` executions shipped ~920 javadoc files (~17 MB uncompressed) that no API measurement uses.
- Decision: Exclude the test tooling from `kadai-test-api` in `rest/kadai-gmt/pom.xml` and drop both javadoc copy executions.
- Rationale: The fix stays local to the app that repackages itself into an executable jar. Narrowing the scopes inside `kadai-test-api` would change a published test module for all of its consumers and is tracked separately in #2359; once that is resolved, the exclusions can be dropped again.
- Consequences: The image loses about a third of its size and carries neither a test framework nor a Docker client; the measured workload is unchanged. As above, this only takes effect with a newly built and re-pinned image.

## Build certification images from a dedicated branch, tagged after the branch
- Date / related: 2026-10-26, #2358, #2378
- Status: accepted
- Context: The certification of KADAI v12.1.0 has to measure the released code plus two fixes (#2360 aligns the GMT app configuration, #2379 checks the health endpoint on the management port) without the unrelated commits that landed on `master` after the release (dependency bumps, snapshot version, pin bumps). The measured subject is a container image, so a pinned `sha-<commit>` of `master` cannot express that state.
- Decision: The certification image is built from a branch cut from the release tag with only those commits cherry-picked (`blauer-engel/<version>`), and pinned with a tag derived from the branch name (`blauer-engel-v12.1.0`) instead of a `sha-<commit>` tag. The workflow builds images for `blue-angel`-style certification branches as well, but publishes `latest` and opens the regular pin bump PR only for the default branch.
- Rationale: A branch-derived tag lets the pinned tag and the measured commit be the same commit — with a `sha-<commit>` pin the tag always points at the parent of the commit that contains the pin. Keeping `latest` and the automatic bump master-only keeps the regular measurements and the quick scenario unchanged.
- Consequences: The image tag is only as immutable as its branch, so a new measurement needs its own branch and tag. The registry digest is recorded in the certification document, which is the actual guarantee of what was measured. Certification branches are kept as permanent git tags so the measured code stays retrievable.

## Generate the test data in a child JVM before the measured app starts
- Date / related: 2026-10-27, #2387
- Status: accepted
- Context: Generating the 100000 tasks inside the measured `kadai-gmt` JVM during `[BOOT]` left the app in `[IDLE]` with a 54.5% larger committed heap (437.8 MB in run `2876eecb` vs 283.4 MB in the pre-seeded run `a0d07483`; `[RUNTIME]` +12.3%, Standard Usage Scenario +9.6%). The generator streams its rows into a bounded COPY buffer, so the 1000 `Task` objects it holds are not the cause: the high allocation rate makes G1 expand the young generation and commit more heap, and the generator's classes, JIT code and metaspace stay loaded afterwards.
- Decision: Run the generator in a short-lived child JVM (`java -Xmx1g -jar /app/kadai-gmt.jar --generate-tasks=N`) from the `app` entrypoint before the measured app is `exec`ed; the child exits once the data is committed. `GENERATE_TASKS` controls the count, `0` disables generation. This partially supersedes "Seed via GMT `setup-commands` rather than a flow step": generation still runs in `[BOOT]`, but no longer as a `setup-commands` step and no longer inside the measured JVM.
- Rationale: The measured app then starts against an already-seeded database, matching the old pre-seeded setup, and the generator's heap, JIT code and class metadata die with the child JVM. Lowering `BATCH_SIZE` would not help, since it only sizes the COPY buffer. Generating before the app starts also avoids the LDAP port conflict that seeding through the API had. The child cannot run without a servlet environment (`WebApplicationType.NONE` fails: `BootWebSecurityConfigurer` requires `HttpSecurity`), so it starts the full app context on ephemeral ports (`--server.port=0`, `--management.server.port=0`).
- Consequences: The `[IDLE]` baseline no longer contains the generator footprint, at the cost of a longer `[BOOT]` (app healthcheck `start_period` raised to 240 s) and a second heap setting for the child JVM. `POST /kadai/api/v1/gmt/tasks` stays for manual seeding and is covered by an integration test. An image that predates this change ignores `--generate-tasks` (Spring accepts it as an unknown property) and generates nothing, so every pinned tag used by a scenario must be rebuilt. The certification image is `blauer-engel-v12.1.0-r2`, built from the certification branch `blauer-engel/v12.1.0-r2`; rebuilding `blauer-engel-v12.1.0` itself would have moved the tag of the already certified run.
