#!/bin/bash

while read line; do
  lines[${#lines[@]}]=$line
done

echo "new String[] {"
for ((i=0; i < ${#lines[@]}; ++i)); do
  echo "    \"${lines[$i]//\"/\\\"}\",";
done
echo "}"

