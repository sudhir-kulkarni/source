#!/bin/bash


java -cp "../../ext-lib/*":"../../export/data-files/":"../../lib/*":"../../config/":"../../export/"  com.persistent.bcsuite.dataStore.Loader $1 $2
