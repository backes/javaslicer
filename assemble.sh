#!/bin/bash

cd `dirname $0`
WORKINGDIR=`pwd`
ASSEMBLY_DIR=$WORKINGDIR/assembly

mkdir -p "$ASSEMBLY_DIR"
rm -f "$ASSEMBLY_DIR"/*.jar

projects=()
hasAssembly=()
num_projects=0
for dir in */pom.xml; do
  projects[$num_projects]=${dir%/pom.xml}
  if grep assembly-plugin $dir >/dev/null; then
    hasAssembly[$num_projects]=1
  fi
  num_projects=$((num_projects+1))
done

if [[ $num_projects -eq 0 ]]; then
  echo "No projects found!"
  exit;
fi

echo "Found $num_projects projects:"
for ((i=0; i<num_projects; ++i)); do
  project=${projects[$i]}
  if [[ "${hasAssembly[$i]}" -eq 1 ]]; then
    echo "  - "$project" (with assembly)"
  else
    echo "  - "$project
  fi
done

if [[ -e ~/.mavenrc ]]; then
  . ~/.mavenrc
fi

echo

echo "Pass 1: run install on all projects:"
for ((i=0; i<num_projects; ++i)); do
  project=${projects[$i]}
  echo -n "  - "$project"...  "
  cd "$WORKINGDIR/$project"
  if mvn -Dmaven.test.skip=true install >/dev/null 2>&1; then
    echo success
  else
    echo failure
  fi
done
echo

echo "Pass 2: run assembly on all projects with assembly descriptors, install else:"
for ((i=0; i<num_projects; ++i)); do
  if [[ "${hasAssembly[$i]}" -eq 1 ]]; then
    TARGET="install assembly:assembly"
  else
    TARGET=install
  fi
  project=${projects[$i]}
  echo "  - "$project" ("$TARGET")..."
  cd "$WORKINGDIR/$project"
  output=()
  TMPFILE=`mktemp /tmp/assembly_XXXXXX`
  if ! mvn -Dmaven.test.skip=true $TARGET >$TMPFILE 2>&1; then
    echo "failed!"
    echo
    cat $TMPFILE
    rm $TMPFILE
    exit
  fi
  rm $TMPFILE
  for jar in target/assembly/*.jar; do
    if [[ -e $jar ]]; then
      echo "Copying $jar to $ASSEMBLY_DIR directory."
      cp $jar "$ASSEMBLY_DIR"
    fi
  done
done

# vim:set encoding=utf-8 fileencoding=utf-8 nomodified :
# vim:set shiftwidth=2 tabstop=2 expandtab smartindent :
