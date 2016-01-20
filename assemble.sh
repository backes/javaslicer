#!/bin/bash

cd `dirname $0`
WORKINGDIR=`pwd`
ASSEMBLY_DIR=$WORKINGDIR/assembly
TMPFILE=`mktemp /tmp/assembly_XXXXXX`

mkdir -p "$ASSEMBLY_DIR"
rm -f "$ASSEMBLY_DIR"/*.jar

projects=()
hasAssembly=()
num_projects=0
for dir in ./pom.xml */pom.xml; do
  if [[ -e $dir ]]; then
    projects[$num_projects]=${dir%/pom.xml}
    if grep assembly-plugin $dir >/dev/null; then
      hasAssembly[$num_projects]=1
    fi
    num_projects=$((num_projects+1))
  fi
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

success=()
num_success=0
echo "Pass 1: run clean and install on all projects iteratively, until all succeed:"
while [[ $num_success != $num_projects ]]; do
  old_num_success=$num_success
  for ((i=0; i<num_projects; ++i)); do
    if [[ ${success[$i]} != 1 ]]; then
      project=${projects[$i]}
      echo -n "  - "$project"...  "
      cd "$WORKINGDIR/$project"
      if mvn -N -Dmaven.test.skip=true clean install >$TMPFILE 2>&1; then
        success[$i]=1
        num_success=$((num_success+1))
        echo success
      else
        echo failure
      fi
    fi
  done
  if [[ $old_num_success == $num_success ]]; then
    echo Error: No more progress, exiting!
    echo Components with errors:
    for ((i=0; i<num_projects; ++i)); do
      if [[ ${success[$i]} != 1 ]]; then
        echo "  - "${projects[$i]}
      fi
    done
    echo
    echo Last error output:
    cat $TMPFILE
    exit 1
  fi
done
echo

echo "Pass 2: run assembly on all projects with assembly descriptors:"
for ((i=0; i<num_projects; ++i)); do
  if [[ "${hasAssembly[$i]}" -ne 1 ]]; then
    continue
  fi
  project=${projects[$i]}
  echo "  - "$project
  cd "$WORKINGDIR/$project"
  output=()
  if ! mvn -Dmaven.test.skip=true assembly:assembly >$TMPFILE 2>&1; then
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

# vim:set fileencoding=utf-8 nomodified :
# vim:set shiftwidth=2 tabstop=2 expandtab smartindent :
