#!/usr/bin/env bash


if [ ! -d "../kafka-proxied" ]; then
  cd ../
  git clone https://github.com/petergdoyle/kafka-proxied.git
  cd -
fi
