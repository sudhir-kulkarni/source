#!/bin/bash
deviceName=$4
token=$2
interval=$3
PID=$1
if [[ $1 = "" || $2 = "" || $3 = "" || $4 = "" ]]; then
echo "No parameter supplied valid sequence is <./filename>  <PID> <token> <interval> <deviceName>"
else
#echo "Gathering memory Statistics..."
pidstat -h  -r -p $PID $interval  > ../../export/data-files/$deviceName.txt 
 #echo "Gathering memory Statistics done..."
./device-statistics-to-db.sh $deviceName $token 
fi