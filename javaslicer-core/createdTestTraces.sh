#!/bin/bash

TRACER=../javaslicer-tracer/tracer.jar
CLASSPATH=target/test-classes
SOURCEDIR=src/test/java/de/unisb/cs/st/javaslicer/tracedCode
PACKAGE=de.unisb.cs.st.javaslicer.tracedCode
TARGETDIR=src/test/resources/traces
DEFAULT_ARGUMENT="1"

for file in $SOURCEDIR/*.java; do
	if [[ ! -e $file ]]; then
		echo "File not found: $file"
	fi
	CLASS=${file#$SOURCEDIR/}
	CLASS=${CLASS%.java}
	TARGETFILE=`echo $CLASS | awk '{ print tolower($0);}'`
	echo -e "\n\nRunning $CLASS..."
	if ! time java -javaagent:$TRACER=tracefile:$TARGETDIR/$TARGETFILE -ea -esa -cp $CLASSPATH $PACKAGE.$CLASS $DEFAULT_ARGUMENT; then
		echo ERROR
		exit 1
	fi
done

