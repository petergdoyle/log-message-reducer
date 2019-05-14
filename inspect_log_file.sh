#!/usr/bin/env bash

log_file_name="$PWD/logs/app.log"
read -e -p "Enter log file name: " -i "$log_file_name" log_file_name

if [ -f $log_file_name ] && [ -s $log_file_name ]; then
 echo "TOTAL: $(wc -l <$log_file_name)" \
  && echo "TRACE: $(grep -ci 'TRACE' $log_file_name)" \
  && echo "WARN: $(grep -ci 'WARN' $log_file_name)" \
  && echo "INFO: $(grep -ci 'INFO' $log_file_name)" \
  && echo "DEBUG: $(grep -ci 'DEBUG' $log_file_name)" \
  && echo "ERROR: $(grep -ci 'ERROR' $log_file_name)" \
  && echo "FATAL: $(grep -ci 'FATAL' $log_file_name)"
fi
