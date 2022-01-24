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

for i in $(seq 1 $((${#BUS[@]} - 2))); do
    echo "Comparing $(basename ${BUS[$i]}) <> $(basename ${BUS[$((i+1))]})..."
    target_file=cumul/$(basename ${BUS[$i]})_$(basename ${BUS[i+1]}).txt
    if [ -f "$target_file" ]; then
        echo "$target_file exists"
    else
        echo "$target_file does not exist"
    fi
    #echo $target_file
    #tmutil compare "${BUS[$i]}" "${BUS[i+1]}" > $(basename ${BUS[$i]})_$(basename ${BUS[i+1]}).txt
done
