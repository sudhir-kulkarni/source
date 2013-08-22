#!/bin/bash

#load the parameters to run the specific test from  config/subscriber-config.properties 
. config/subscriber-config.properties 

#parameter for subscriber
MIN=$1
increment=$2
MAX=$3
key=$4
groupName=$5
token=$6

#check if all parameters are provided for test
if [[ $1 = "" || $2 = "" || $3 = "" || $4 = "" || $5 = "" || $6 = "" ]]; then
echo "No parameter supplied :  valid sequence is <./run-subscriber-vary-sub.sh> <MIN-sub> <increment-By> <MAX-sub> <key> <groupName> <token>"
else

#parameter for sar test 
interval=1
totalRun=$(( $MAX / $increment )) 
runForSec=$(( $totalRun * $shutdownDelay + 5 ))

#start the sar test for specified sec & o/p of sar is stored in  "../../export/data-files/" dir
#sar test is disabled because all statistics data is captured by other utilities
#to enable sar test uncomment following two lines 

#echo "sar-test for  varying-sub started [$runForSec sec]..."
#./subscriber-sar-export.sh $token $key $interval $runForSec  > ../../export/data-files/sar-temp.txt   &

 #start the iteration upto MAX limit 
echo "Subscriber for varying-sub-test started..."
for (( j=$MIN; j <= $MAX; j=j+$increment )) 
  do
 
 #copy the  test specific config parameter values from "subscriber-config.tmpl" to  "subscriber-config.xml"
 cp ../../config/subscriber-config.tmpl ../../config/subscriber-config.xml
 sed -i 's/@shutdownDelay/'$shutdownDelay'/g' ../../config/subscriber-config.xml 
 sed -i 's/@dumpDetails/'$dumpDetails'/g'  ../../config/subscriber-config.xml 
 sed -i 's/@maxSubscriber/'$j'/g' ../../config/subscriber-config.xml 
 sed -i 's/@maxTopics/'$maxTopics'/g' ../../config/subscriber-config.xml 
 
#Reinitialize token value 
token=$6-$j

#run the extractor utility which actually receives the messages.
#loads the external jars from "ext-lib" , utility jars from "lib" & configuration parameter from .tmpl file inside  "config" dir 
 java -d64 -Xms4048M -Xmx14048M -cp  "../../ext-lib/*":"../../ext-lib/rti/*":"../../export/data-files/":"../../lib/mbsuite-addons.jar":"../../config/":"../../lib/mbsuite-subscriber-base.jar":"../../export/"    com.persistent.bcsuite.process.Extractor $key $groupName $token $iteration  >> logs/run-subscriber-vary-sub.log &
	
	#get the PID of extractor process
	pid=$!
	#calls the CPU statistics script with required parameter & dump data to "log/" dir
	./run-cpu-statistics.sh $pid $token $interval subscriber-cpu >> logs/subscriber-cpu.log &
	#get the PID of CPU statistics process 
	cpuPid=$!
	#calls the MEMORY statistics script with required parameter & dump data to "log/" dir
	./run-memory-statistics.sh $pid $token $interval subscriber-memory >> logs/subscriber-memory.log &
	#get the PID of MEMORY statistics process
	memoryPid=$!
	#calls the DISK statistics script with required parameter & dump data to "log/" dir
	./run-disk-statistics.sh $pid $token $interval subscriber-disk >> logs/subscriber-disk.log &
	#get the PID of DISK statistics process
	diskPid=$!
	#calls the NETWORK statistics script with required parameter & dump data to "log/" dir
	./run-network-statistics.sh $((shutdownDelay+65)) $token $interval subscriber-network >> logs/subscriber-network.log &
	#get the PID of NETWORK statistics process
	networkPid=$!
	#calls the HEAP statistics script with required parameter & dump data to "log/" dir
	./run-heap-statistics.sh $pid $token $interval $shutdownDelay  subscriber-heap >> logs/subscriber-heap.log 2>&1 &
	#get the PID of HEAP statistics process
	heapPid=$!

	echo "Receiving messages.." 
	for (( ; ; ))
	do
	#check if the generator,CPU,MEMORY,DISK,NETWORK or HEAP process is alive
	IsAlivePub=`ps -p $pid | grep $pid -c`  
	IsAliveCpu=`ps -p $cpuPid | grep $cpuPid -c`
	IsAliveMemory=`ps -p $memoryPid | grep $memoryPid -c`
	IsAliveDisk=`ps -p $diskPid | grep $diskPid -c`
	IsAliveNetwork=`ps -p $networkPid | grep $networkPid -c`
    IsAliveHeap=`ps -p $heapPid | grep $heapPid -c`

	#if processes are dead then break the iteration
   	if [[ $IsAlivePub = 0 && $IsAliveCpu = 0 && $IsAliveMemory = 0 && $IsAliveDisk = 0 && $IsAliveNetwork = 0 && $IsAliveHeap = 0 ]]; then
	break
	fi	
	done
       echo "varying-sub[$j] finish.."
       read -p "Press [Enter] key to continue.. or Ctr-C to quit"
  done
fi



