#!/bin/sh

# Copyright (c) 2006 Martin Burger. All rights reserved.

JAVACMD=`which java`
MAINCLASS=de.unisb.cs.st.AppCli

# ------------------------------------------------------------------------------

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVACMD is not defined correctly. It has to point to the Java binary."
  echo "Thus, we cannot execute $JAVACMD"
  exit 1
fi

currentdir=`pwd`

# TODO may not work with links
TOOL_HOME=`dirname "$0"`/..

# -> fully qualified
TOOL_HOME=`cd "$TOOL_HOME" && pwd`

cd "$currentdir"

LIBDIR=${TOOL_HOME}/lib

for jar in $( find $LIBDIR -name "*.jar" | sort -r )
do
    CLASSPATH=$jar:${CLASSPATH}
done

# remove last ':'
CLASSPATH=${CLASSPATH:0:${#CLASSPATH} - 1}

if [ "$1" != "" ] ; then
    echo Prepending to classpath: ${1}
    CLASSPATH=${1}:${CLASSPATH}
fi

exec "$JAVACMD" \
    -classpath "${CLASSPATH}" \
    $MAINCLASS
