#!/bin/bash

# space separated list of excludes
EXCLUDE="javaslicer_*.tar.bz2 create_archive.sh add_license_header.pl checkstyle-license.txt evaluation"

if [[ ! -d ../java-slicer ]]; then
  echo Please execute this script inside the java-slicer directory.
  exit 1
fi

if [[ -e assembly || -e javaslicer-core/target ]]; then
  echo Please checkout a clean directory, so that assembly and compiled files are not added to the archive
  exit 1
fi

DATE=`date +%Y%m%d`
FILENAME=javaslicer_${DATE}.tar.bz2

if [[ -e $FILENAME ]]; then
  echo File already exists: $FILENAME
  exit 1
fi

cd ..
COMMAND="tar cj"
for e in $EXCLUDE; do
	COMMAND=$COMMAND" --exclude java-slicer/"$e
done
COMMAND=$COMMAND" -f java-slicer/"$FILENAME" java-slicer"
echo "Command: $COMMAND"
if ! $COMMAND; then
  echo Could not create archive...
  exit 1
fi
cd java-slicer

echo
echo Created archive file: $FILENAME
echo -n in directory
pwd
du -sm $FILENAME
echo

TAGNAME="archive_"$DATE
echo Creating git tag $TAGNAME...
git tag $TAGNAME

echo Do not forget to push the tag:
echo git push $TAGNAME

