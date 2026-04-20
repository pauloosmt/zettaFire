#!/bin/sh
set -e

if [ "${PIPELINE_ONCE:-false}" = "true" ]; then
  exec python3 main.py --once
fi

exec python3 main.py --interval-seconds "${PIPELINE_INTERVAL_SECONDS:-1800}"
