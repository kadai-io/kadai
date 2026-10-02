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
- Consequences: More realistic measurement behavior and a schema-compatible setup; requires a generation step before the measured flow.

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
