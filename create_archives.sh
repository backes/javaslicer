#!/bin/bash

# space separated list of excludes
EXCLUDE="javaslicer_*.tar.bz2 create_archives.sh add_license_header.pl evaluation"

if [[ ! -d ../javaslicer || ! -f pom.xml || ! -d javaslicer-core ]]; then
  echo Please execute this script inside the javaslicer directory.
  exit 1
fi

if [[ -e assembly || -e javaslicer-core/target ]]; then
  echo Please checkout a clean directory, so that assembly and compiled files are not added to the archive
  exit 1
fi

DATE=`date +%Y%m%d`
FILENAME=javaslicer_src_${DATE}.tar.bz2
FILENAME_BIN=javaslicer_jar_${DATE}.tar

if [[ -e $FILENAME ]]; then
  echo File already exists: $FILENAME
  exit 1
fi
if [[ -e $FILENAME_BIN ]]; then
  echo File already exists: $FILENAME_BIN
  exit 1
fi
if [[ -e ${FILENAME_BIN}.bz2 ]]; then
  echo File already exists: ${FILENAME_BIN}.bz2
  exit 1
fi

cd ..
COMMAND="tar cj"
for e in $EXCLUDE; do
	COMMAND=$COMMAND" --exclude javaslicer/"$e
done
COMMAND=$COMMAND" -f javaslicer/"$FILENAME" javaslicer"
if ! $COMMAND; then
  echo Could not create archive...
  exit 1
fi
cd javaslicer

if [[ -d assembly ]]; then
  echo Error: assembly directory already exists
  exit 1
fi

if ! ./assemble.sh; then
  echo Could not build jar files
  exit 1
fi

if [[ ! -d assembly ]]; then
  echo Error: assembly directory not present
  exit 1
fi

cd assembly
if ! tar -cf ../$FILENAME_BIN *.jar; then
  echo Error creating jar archive
  exit 1
fi
cd ..
if ! tar -rf $FILENAME_BIN README.md; then
  echo Error adding README.md
  exit 1
fi

if ! bzip2 $FILENAME_BIN; then
  echo Error compressing jar archive
  exit 1
fi

if [[ ! -e ${FILENAME_BIN}.bz2 ]]; then
  echo Hmm, ${FILENAME_BIN}.bz2 is not there after compressing $FILENAME_BIN...
  exit 1
fi

echo
echo Created archive files: $FILENAME ${FILENAME_BIN}.bz2
echo -n in directory
pwd
du -sh $FILENAME ${FILENAME_BIN}.bz2
echo



TAGNAME="archive_"$DATE
echo Creating git tag $TAGNAME...
echo git tag $TAGNAME
git tag $TAGNAME

echo Do not forget to push the tag:
echo git push $TAGNAME

