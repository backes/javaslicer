#!/bin/bash

CLI_MODULE=cooltool-cli

## resolve links - $0 may be a link to my home
PRG="$0"

# need this for relative symlinks
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="`dirname "$PRG"`/$link"
  fi
done

saveddir=`pwd`

HOME_DIR=`dirname "$PRG"`

# make it fully qualified
HOME_DIR=`cd "$HOME_DIR" && pwd`

cd $HOME_DIR

mvn install

cd ./${CLI_MODULE}/

mvn assembly:assembly

cd $saveddir

echo ''
echo Finished:
ls -lT ${HOME_DIR}/${CLI_MODULE}/target/assembly/*

