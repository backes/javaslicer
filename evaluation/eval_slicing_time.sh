#!/bin/bash

if [[ -n "$SUN_DIR" && -e $SUN_DIR ]]; then
  true # everything ok
elif [[ -e /usr/lib/jvm/java-6-sun ]]; then
  SUN_DIR=/usr/lib/jvm/java-6-sun
elif [[ -e /usr/lib/jvm/jdk1.6.0 ]]; then
  SUN_DIR=/usr/lib/jvm/jdk1.6.0
elif [[ -e /opt/sun-jdk-1.6.0.07 ]]; then
  SUN_DIR=/opt/sun-jdk-1.6.0.07
elif [[ -e /opt/sun-jdk-1.6.0.10 ]]; then
  SUN_DIR=/opt/sun-jdk-1.6.0.10
else
  echo "No java 6 directory found."
  exit 1
fi

if [[ $# -lt 1 ]]; then
  echo Please specify the trace files to use as parameters.
  exit 1
fi

TRACEFILES=( $* )

MEMORY="7g"

resultfilenr=0
while [[ -e "slice_result_$resultfilenr" ]]; do
  resultfilenr=$((resultfilenr+1))
done
resultfile="slice_result_"$resultfilenr

echo writing results to $resultfile

printf '"tracefile","tracesize","criterionNr","criterion","slicesize","msec"\n' >>$resultfile


for (( i=0; i < ${#TRACEFILES[@]}; ++i )); do
  tracefile=${TRACEFILES[$i]}
  if [[ -e $tracefile.txt ]]; then
    s=`wc -l <$tracefile.txt`
    echo "txt file $tracefile.txt already exists (size: $s)."
    tracesize[$i]=$s
  else
    echo creating txt file $tracefile.txt
    time $SUN_DIR/bin/java -Xmx$MEMORY -jar tracer_1000th.jar $tracefile >$tracefile.txt
    s=`wc -l <$tracefile.txt`
    echo "created txt file $tracefile.txt (size: $s)."
    tracesize[$i]=$s
  fi
done

while true; do
  for (( i=0; i < ${#TRACEFILES[@]}; ++i )); do
    tracefile=${TRACEFILES[$i]}
    echo using tracefile $tracefile...
  
    starttime=`date +'%s*1000+%N/1000000' | sed 's/+0*/+/'`

    s=${tracesize[$i]}
    criterionNr=`perl -e "print 1+int(rand($s))"`
    echo criterion Nr: $criterionNr
    criterion=`head -n $criterionNr $tracefile.txt | tail -n 1 | sed 's/^.*: //; s/ .*$//'`"(1)"
    echo criterion: $criterion

    time $SUN_DIR/bin/java -Xmx$MEMORY -jar slicer.jar $tracefile 1 $criterion >slice.txt
    
    endtime=`date +'%s*1000+%N/1000000' | sed 's/+0*/+/'`
    millis=$(( ($endtime) - ($starttime) ))

    slicesize=`wc -l <slice.txt`
  
    echo "## slice size: $slicesize, $millis milli seconds"
    printf '"%s",%d,%d,"%s",%d,%d\n' $tracefile $s $criterionNr $criterion $slicesize $millis >>$resultfile
  
  done
done

