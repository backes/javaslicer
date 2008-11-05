#!/bin/bash

KAFFE_DIR=${KAFFE_DIR:-/usr/local/kaffe}
if [[ -n "$SUN_DIR" && -e $SUN_DIR ]]; then
  true # everything ok
elif [[ -e /usr/lib/jvm/java-6-sun ]]; then
  SUN_DIR=/usr/lib/jvm/java-6-sun
elif [[ -e /usr/lib/jvm/jdk1.6.0 ]]; then
  SUN_DIR=/usr/lib/jvm/jdk1.6.0
elif [[ -e /opt/sun-jdk-1.6.0.10 ]]; then
  SUN_DIR=/opt/sun-jdk-1.6.0.10
else
  echo "No java 6 directory found."
  exit 1
fi

MEMORY="2000m"
RUNS=5

if [[ ! -d $KAFFE_DIR ]]; then
  echo Kaffe directory does not exist: $KAFFE_DIR
  exit 1
fi

dir=`pwd`

if [[ $# -eq 1 ]]; then
  cases[0]=$1
elif [[ $# -eq 0 ]]; then
  for i in *; do
    if [[ -d $i && -e $i/my_criterion && -e $i/jslice_criterion && -e $i/commandline ]]; then
      cases[${#cases[@]}]=$i
    fi
  done
else
  echo "Either 0 parameter, or exactly 1 (the test case to run)"
  exit 1
fi

tmpdir=`mktemp -d`

echo -n "Executing ${#cases[@]} test cases:"
for (( i = 0; i < ${#cases[@]}; ++i )); do
  echo -n " ${cases[$i]}"
done
echo

for (( i = 0; i < ${#cases[@]}; ++i )); do
  case=${cases[$i]}

  echo --------------------------------------
  echo Executing: $case

  for (( run = 1; run <= $RUNS; ++run )); do
    echo "Run $run..."

    cp $case/* $tmpdir
  
    cd $tmpdir
  
    # my slicer:
  
    echo "## Slice computation using my slicer:"
    javafiles=( `find . -name "*.java"` )
    if [[ ${#javafiles[@]} -gt 0 ]]; then
      echo Compiling ${#javafiles[@]} source files...
      $SUN_DIR/bin/javac ${javafiles[@]}
    fi

    criterion=`cat my_criterion`
  
    starttime=`date +'%s*1000+%N/1000000' | sed 's/+0*/+/'`
  
    echo Running program and tracing...
    $SUN_DIR/bin/java -Xmx$MEMORY -javaagent:$dir/tracer.jar=logfile:trace.log `./commandline $dir`
  
    echo Computing slice...
    (
    $SUN_DIR/bin/java -Xmx$MEMORY -jar $dir/slicer.jar trace.log 1 $criterion |
      tee myslicer_out |
      sed -nr 's/^([^ :]+\.)?([^ :.]+)\.([^ :.]+):([0-9]+) .*$/\2.java \4/ p'
    # always add the slicing criterion into the slice, because jslice does the same:
    sed -nr 's/^([^ :]+\.)?([^ :.]+)\.([^ :.]+):([0-9]+)/\2.java \4/ p' my_criterion
    ) |
      sort |
      uniq >unified_mine
  
    endtime=`date +'%s*1000+%N/1000000' | sed 's/+0*/+/'`
    millis=$(( ($endtime) - ($starttime) ))
  
    echo "My slicer: $millis msec"
    times_mine[$run]=$millis
  
  
    # JSlice
  
    echo "## Slice computation using JSlice:"
    classfiles=( `find . -name "*.class"` )
    if [[ ${#classfiles[@]} -gt 0 ]]; then
      rm ${classfiles[@]}
    fi
    javafiles=( `find . -name "*.java"` )
    if [[ ${#javafiles[@]} -gt 0 ]]; then
      echo Compiling ${#javafiles[@]} source files...
      $KAFFE_DIR/bin/javac ${javafiles[@]}
    fi
  
    starttime=`date +'%s*1000+%N/1000000' | sed 's/+0*/+/'`
  
    echo Running program and computing slice...
    $KAFFE_DIR/bin/java -mx $MEMORY -noclassgc -slicing -foreclipse jslice_criterion jslice_result `./commandline $dir`
    
    cat jslice_result |
      awk '/.java$/ { file=$0; getline; printf "%s %s\n", file, $0; }' |
      sort |
      uniq >unified_jslice
  
    endtime=`date +'%s*1000+%N/1000000' | sed 's/+0*/+/'`
    millis=$(( ($endtime) - ($starttime) ))
  
    echo "JSlice: $millis msec"
    times_jslice[$run]=$millis
  
    if ! diff -U0 unified_mine unified_jslice >diff; then
      echo "There is a diff in the slices:"
      egrep -v '^@@' diff
      echo
      echo ENTER to continue
      read unused
    else
      echo ENTER to continue
      read unused
    fi
  
    cd $dir
  
    rm -rf $tmpdir/*
  done

  printf '"%s",' $case >>$tmpdir/result
  sum=0
  for (( run = 1; run <= $RUNS; ++run )); do
    printf '%d,' ${times_mine[$run]} >>$tmpdir/result
    sum=$(( sum + ${times_mine[$run]} ))
  done
  printf "%d," $(( sum / RUNS )) >>$tmpdir/result
  sum=0
  for (( run = 1; run <= $RUNS; ++run )); do
    printf '%d,' ${times_jslice[$run]} >>$tmpdir/result
    sum=$(( sum + ${times_jslice[$run]} ))
  done
  printf "%d\n" $(( sum / RUNS )) >>$tmpdir/result
done

echo "Ready."
echo
echo "Result (CSV, also stored in \"result\"):"

printf "Testcase,"
for (( run = 1; run <= $RUNS; ++run )); do
  printf '"mine, run %d",' $run
done
printf '"mine, avg",'
for (( run = 1; run <= $RUNS; ++run )); do
  printf '"jslice, run %d",' $run
done
printf '"jslice, avg"\n'

mv $tmpdir/result result
cat result

rm -rf $tmpdir

