#!/bin/bash

set -e
set -x

BASE_URL=https://kadai-io.azurewebsites.net/kadai
MAX_RETRIES=5
RETRY_DELAY=10  # in seconds

retry_curl() {
  local url=$1
  local retries=$MAX_RETRIES
  while [ $retries -gt 0 ]; do
    status_code=$(curl -sw "%{http_code}" -o /dev/null "$url")
    if [ "$status_code" -eq 200 ]; then
      return 0
    fi
    echo "Retrying... ($((MAX_RETRIES - retries + 1))/$MAX_RETRIES) - Received status code $status_code for $url"
    retries=$((retries - 1))
    sleep $RETRY_DELAY
  done
  return 1
}

retry_curl "$BASE_URL/api-docs"
retry_curl "$BASE_URL/swagger-ui/index.html"
for module in kadai-core kadai-spring; do
  retry_curl "$BASE_URL/docs/java/$module/index.html"
done
