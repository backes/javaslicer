#!/bin/bash

TRACER=../javaslicer-tracer/target/assembly/tracer.jar
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
	TARGETPATH=$TARGETDIR/$TARGETFILE
	if [[ -e $TARGETPATH ]]; then
		echo $TARGETFILE already exists.
	else
		echo -e "\n\nRunning $CLASS..."
		STARTTIME=`date +%s`
		echo executing: java -javaagent:$TRACER=tracefile:$TARGETPATH -ea -esa -cp $CLASSPATH $PACKAGE.$CLASS $DEFAULT_ARGUMENT
		java -Xmx2g -javaagent:$TRACER=tracefile:$TARGETPATH -ea -esa -cp $CLASSPATH $PACKAGE.$CLASS $DEFAULT_ARGUMENT
		ENDTIME=`date +%s`
		echo "Realtime seconds: "$((ENDTIME - STARTTIME))
	fi
done

