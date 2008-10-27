#!/bin/bash

KAFFE_DIR=${KAFFE_DIR:-/usr/local/kaffe}

if [[ ! -d $KAFFE_DIR ]]; then
  echo Kaffe directory does not exist: $KAFFE_DIR
  exit 1
fi

if [[ $# -eq 1 ]]; then
  cases[0]=$1
elif [[ $# -eq 0 ]]; then
  for i in *; do
    if [[ -d $i && -e $i/criterion ]]; then
      cases[${#cases[@]}]=$i
    fi
  done
else
  echo "Either 0 parameter, or exactly 1 (the test case to run)"
  exit 1
fi

tmpdir=`mktemp -d`

echo -n "Executing ${#cases[@]} test cases:"
for (( i = 0; i < ${#cases[@]}; ++i )); do
  echo -n " ${cases[$i]}"
done
echo

for (( i = 0; i < ${#cases[@]}; ++i )); do
  case=${cases[$i]}

  echo --------------------------------------
  echo Executing: $case

  #java -noclassgc -slicing -foreclipse criteria jslice_result de.unisb.cs.st.javaslicer.tracedCode.Simple1 1
  rm -rf $tmpdir/*
done

