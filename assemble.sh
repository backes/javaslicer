#!/bin/bash

# we have to do this by hand: call mvn assembly:assembly and copy over the resulting "fat" jar
CWD=$(pwd)
SUCCESS=0

cd $CWD/javaslicer-tracer && mvn assembly:assembly && cp -v target/tracer.jar "$CWD" && SUCCESS=$((SUCCESS + 1))

cd $CWD/javaslicer-traceReader && mvn assembly:assembly && cp -v target/traceReader.jar "$CWD" && SUCCESS=$((SUCCESS + 1))

cd $CWD/javaslicer-core && mvn assembly:assembly && cp -v target/slicer.jar "$CWD" && SUCCESS=$((SUCCESS + 1))

cd $CWD

echo
echo Successfully copied $SUCCESS/3 jar files.

if [[ $SUCCESS -lt 3 ]]; then exit 1; fi
exit 0

