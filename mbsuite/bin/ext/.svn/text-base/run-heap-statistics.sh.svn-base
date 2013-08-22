#!/bin/bash
PID=$1
token=$2
interval=$3
runForSec=$4
deviceName=$5


if [[ $1 = "" || $2 = "" || $3 = "" || $4 = "" || $5 = "" ]]; then
echo "No parameter supplied valid sequence is <./filename>  <PID> <token> <interval> <runForSec> <deviceName>"
else
#echo "Gathering HEAP Statistics..."
./jstat-heap-statistics.sh -p $PID -d $runForSec -i $interval  > ../../export/data-files/$deviceName.txt 2>&1
#echo "Gathering HEAP Statistics done..."
./device-statistics-to-db.sh $deviceName $token 
fi