# Green Metrics Tool

We are using the [Green Metrics Tool (GMT)](https://www.green-coding.io/products/green-metrics-tool/) to measure the resource and energy consumption of typical usage scenarios of KADAI.

GMT Documentation: <https://docs.green-coding.io>

## Local Execution (Testing)

Local execution is only relevant for testing purposes. To make representative measurements, use the [Measurement Cluster](#measurement-cluster-real-measurements) (see below).

Install GMT: <https://docs.green-coding.io/docs/installation/>

Start GMT (ensure you are in the directory where GMT was installed):

1. `docker compose -f docker/compose.yml up -d`
2. `source venv/bin/activate`

You can either run a local measurement using a cloned KADAI repository on your local filesystem or using the remote URI:

* using cloned repository (change the path "$HOME/kadai" if necessary):
  
  ```sh
  python3 runner.py --name "KADAI REST Spring Example Application" --uri "$HOME/kadai" --filename "qa/gmt/usage_scenario.yml" --skip-unsafe --skip-optimizations --dev-no-system-checks --dev-no-sleeps
  ```

* using remote URI:
  
  ```sh
  python3 runner.py --name "KADAI REST Spring Example Application" --uri "https://github.com/kadai-io/kadai" --filename "qa/gmt/usage_scenario.yml" --skip-unsafe --skip-optimizations --dev-no-system-checks --dev-no-sleeps
  ```

Used [runner switches](https://docs.green-coding.io/docs/measuring/runner-switches/):

* `--skip-unsafe`: Skips unsafe compose contents like ports mappings.
* `--skip-optimizations`: Skips the creation of potential optimization recommendations (not relevant for testing the usage scenario).
* `--dev-no-system-checks`: Skips checking the system if the GMT can run (not relevant for testing the usage scenario).
* `--dev-no-sleeps`: Removes all sleeps (measurement run is faster, but resulting measurement data will be skewed).

## Measurement Cluster (Real Measurements)

A new measurement on the measurement cluster can be triggered via:

* the UI: <https://metrics.green-coding.io/request.html>
* the API: <https://api.green-coding.io/docs#/default/software_add_v1_software_add_post>

For easier use of the API a Python script can be used: [submit_software.py](https://github.com/green-coding-solutions/gmt-helpers/blob/main/api/submit_software.py)

To compare energy efficiency, we use the machine "CO2 Benchmarking (DVFS OFF, TB OFF, HT OFF) * TX1330 M2" (details can be found in the [GMT documentation](https://docs.green-coding.io/docs/measuring/measurement-cluster/)).

## Blauer Engel Certification

For the "Blauer Engel" certification we use the following setup:

* GMT for measuring the resource and energy consumption
* [k6](https://k6.io/) for the automation
* PostgreSQL as the database for KADAI
* [KADAI Spring Example App](https://github.com/kadai-io/kadai/tree/master/lib/kadai-spring-example) (slightly modified) as the target subject
* Phases:
  * **Warm-up:** stabilizes JIT/caches.
  * **Pause:** creates a clean visual/temporal separation in GMT graphs.
  * **Standard Usage:** runs the measured scenario.
      `--log-format=raw`, `--log-output=stdout`, and `read-notes-stdout: true` enable timestamped notes so GMT can align k6 steps with energy/time series.

The measurement setup is defined in a **GMT usage scenario file**: [usage_scenario.yml](./usage_scenario.yml). 

### Phases

* **Warm-up**
  `k6/WarmUpSzenario.js` — primes the JVM (JIT) and caches so that the subsequent measurement reflects steady-state behavior.

* **Pause between warm-up and standard usage scenario**
  `k6/PauseBeforeMeasurement.js` — provides a clearly separated time window between phases (useful for charts/notes and avoiding cross-contamination of metrics).

* **Standard usage scenario (measurement)**
  `k6/NutzungsszenarioBlauerEngel.js` — realistic, parallel-user workflow covering the everyday task lifecycle (create, read, edit, assign/claim, move, comment, complete), workbasket operations (search/open), and a single monitoring report. Steps are synchronized with short gaps to aid interpretation.
