#!/bin/bash

source DEPLOY/config.sh

# Do it in the log directory
logDir="log"
rm -rf $logDir
mkdir $logDir
cd $logDir

echo "Gathering logs from slaves in $logFilename"
data_time=$(date '+%d/%m/%Y %H:%M:%S')
echo $data_time > $logFilename
for c in ${computers[@]}; do
    scp "$login@$c:$remoteFolder$logFilename" "$c-$logFilename"
    echo "---------- Log from $c ----------" >> $logFilename
    echo >> $logFilename
    cat "$c-$logFilename" >> $logFilename
    echo >> $logFilename
done
echo "Adding the log from master to $logFilename"
scp "$login@$master:$remoteFolder$logFilename" "$master-$logFilename"
echo "---------- Log from master ($master) ----------\n" >> $logFilename
cat "$master-$logFilename" >> $logFilename