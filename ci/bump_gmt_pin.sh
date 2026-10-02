#!/bin/bash
set -e # fail fast

#H Usage:
#H %FILE% -h | %FILE% --help
#H
#H Updates the pinned kadai-gmt image in <scenario-file> to sha-$GITHUB_SHA
#H and opens a pull request against master.
#H
#H Requirements:
#H   GH_TOKEN, IMAGE_NAME, GITHUB_SHA
# Arguments:
#   $1: path to the GMT usage scenario file

function helpAndExit() {
  grep "^#H" "$0" | cut -c4- | sed -e "s/%FILE%/$(basename "$0")/g"
  exit "${1:-0}"
}

function main() {
  [[ "$1" == '-h' || "$1" == '--help' ]] && helpAndExit 0
  local scenario_file="${1:?usage: bump_gmt_pin.sh <scenario-file>}"
  : "${GH_TOKEN:?GH_TOKEN must be set}"
  : "${IMAGE_NAME:?IMAGE_NAME must be set}"
  : "${GITHUB_SHA:?GITHUB_SHA must be set}"

  git config user.name "github-actions[bot]"
  git config user.email "41898282+github-actions[bot]@users.noreply.github.com"

  git fetch origin master
  git checkout -B master origin/master
  sed -i -E "s#(${IMAGE_NAME}:sha-)[0-9a-f]+#\1${GITHUB_SHA}#" "$scenario_file"
  git add "$scenario_file"
  if git diff --cached --quiet; then
    echo "Pinned image is already up to date."
    exit 0
  fi

  # Deterministic branch: re-running for the same SHA force-pushes the branch and
  # updates the already-existing PR instead of creating a duplicate.
  local branch="chore/bump-gmt-pin-${GITHUB_SHA}"
  git checkout -B "$branch"
  git commit -m "Bump pinned kadai-gmt image for regular GMT measurements"
  git push --force origin "$branch"

  if ! gh pr create --base master --head "$branch" \
    --title "chore: bump pinned kadai-gmt image to sha-${GITHUB_SHA}" \
    --body "Automated bump of the pinned image in the regular GMT scenario.

Image: \`${IMAGE_NAME}:sha-${GITHUB_SHA}\`, built and published by [this workflow run](${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}/actions/runs/${GITHUB_RUN_ID})." \
    --label "chore"; then
    echo "A pull request for '$branch' already exists; branch has been updated."
  fi
}

main "$@"
