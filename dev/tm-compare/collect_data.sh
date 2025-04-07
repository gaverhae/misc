#!/usr/bin/env bash

set -euo pipefail

format () {
    while IFS= read -r line; do
        echo "[$(date -Is)] $1: $line"
    done
}
exec 1> >(format out >&1)
exec 2> >(format err >&2)

BUS=(/Volumes/.timemachine/*/*/*.backup)
tmp=$(mktemp)

for i in $(seq 1 $((${#BUS[@]} - 2))); do
    target_file=data/$(basename ${BUS[$i]})_$(basename ${BUS[i+1]}).txt.gz
    if [ -f "$target_file" ]; then
        echo "$target_file exists; skipping"
    else
        echo "Comparing $(basename ${BUS[$i]}) <> $(basename ${BUS[i+1]})..."
        tmutil compare "${BUS[$i]}" "${BUS[i+1]}" | gzip -9 > $tmp
        mv $tmp $target_file
    fi
done
