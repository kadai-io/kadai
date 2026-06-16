# Scripts 

## submit_software.py

To submit a new software to GMT you can use the Python script [submit_software.py](submit_software.py).
Source of the script: <https://github.com/green-coding-solutions/gmt-helpers/blob/main/api/submit_software.py>

**Provide secrets:**

```sh
cp .env.example .env

# edit .env

source .env
```

**Example commands:**

```sh
# List active machines
python submit_software.py list-machines

# Submit a one-off run
python submit_software.py --token $GMT_TOKEN submit \
   --name "KADAI REST Spring Example Application - Blauer Engel Szenario" \
   --repo-url "https://github.com/kadai-io/kadai" \
   --machine-id 14 \
   --schedule-mode one-off \
   --email $EMAIL_ADDRESS \
   --filename qa/gmt/usage_scenario_blue_angel.yml \
   --branch master


```

The machine-id should correspond to the available "CO2 Benchmarking" machine.
