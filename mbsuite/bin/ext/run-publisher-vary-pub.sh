#!/bin/bash

#load the parameters to run the specific test from  config/publisher-config.properties 
. config/publisher-config.properties 

#parameter for publisher
MIN=$1
increment=$2
MAX=$3
key=$4
groupName=$5
token=$6

#check if all parameters are provided for test
if [[ $1 = "" || $2 = "" || $3 = "" || $4 = "" || $5 = "" || $6 = "" ]]; then
echo "No parameter supplied :  valid sequence is <./run-publisher-vary-pub.sh> <MIN-pub> <increment-By> <MAX-pub> <key> <groupName> <token>"
else

#parameter for sar test 
interval=1
totalRun=$(( $MAX / $increment )) 
runForSec=$(( $totalRun * $runSec + 5 ))

#start the sar test for specified sec & o/p of sar is stored in  "../../export/data-files/" dir

#sar test is disabled because all statistics data is captured by other utilities
#to enable sar test uncomment following two lines 

#echo "sar-test for  varying-pub started [$runForSec sec]..."
#./publisher-sar-export.sh $token $key $interval $runForSec  > ../../export/data-files/publisher-sar-temp.txt   &

#start the iteration upto MAX limit  
echo "Publisher for varying-pub-test started..."
for (( j=$MIN; j <= $MAX; j=j+$increment )) 
  do

 #copy the  test specific config parameter values from "publisher-config.tmpl" to  "publisher-config.xml"
 cp ../../config/publisher-config.tmpl ../../config/publisher-config.xml
 sed -i 's/@runSec/'$runSec'/g' ../../config/publisher-config.xml 
 sed -i 's/@msgSize/'$msgSize'/g' ../../config/publisher-config.xml 
 sed -i 's/@maxPublisher/'$j'/g' ../../config/publisher-config.xml 
 sed -i 's/@dumpDetails/'$dumpDetails'/g' ../../config/publisher-config.xml 
 sed -i 's/@maxTopics/'$maxTopics'/g' ../../config/publisher-config.xml 

#Reinitialize token value
token=$6-$j

#run the generator utility which actually sends the messages.
#loads the external jars from "ext-lib" , utility jars from "lib" & configuration parameter from .tmpl file inside  "config" dir 
	  java -d64 -Xms4048M -Xmx14048M -cp  "../../ext-lib/*":"../../ext-lib/rti/*":"../../export/data-files/*":"../../lib/*":"../../config/":"../../export/"  com.persistent.bcsuite.process.Generator $key $groupName $token $iteration >> logs/run-publisher-vary-pub.log &
	 
	#get the PID of generator process
	pid=$!
	#calls the CPU statistics script with required parameter & dump data to "log/" dir
	./run-cpu-statistics.sh $pid $token $interval publisher-cpu >> logs/publisher-cpu.log &
	#get the PID of CPU statistics process 
	cpuPid=$!
	#calls the MEMORY statistics script with required parameter & dump data to "log/" dir
	./run-memory-statistics.sh $pid $token $interval publisher-memory >> logs/publisher-memory.log &
	#get the PID of MEMORY statistics process
	memoryPid=$!
	#calls the DISK statistics script with required parameter & dump data to "log/" dir
	./run-disk-statistics.sh $pid $token $interval publisher-disk >> logs/publisher-disk.log &
	#get the PID of DISK statistics process
	diskPid=$!
	#calls the NETWORK statistics script with required parameter & dump data to "log/" dir
	./run-network-statistics.sh $((runSec+5)) $token $interval publisher-network >> logs/publisher-network.log &
	#get the PID of NETWORK statistics process
	networkPid=$!
	#calls the HEAP statistics script with required parameter & dump data to "log/" dir
	./run-heap-statistics.sh $pid $token $interval $runSec  publisher-heap >> logs/publisher-heap.log 2>&1 &
	#get the PID of HEAP statistics process
	heapPid=$!
	
	echo "Sending messages.." 
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
       echo "varying-pub[$j] finish.."
       read -p "Press [Enter] key to continue.. or Ctr-C to quit"
  done
fi

