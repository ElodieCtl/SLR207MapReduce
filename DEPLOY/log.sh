#!/bin/bash

source DEPLOY/config.sh

# Read computers from file
readarray -t available < $AVAILABLE_COMPUTERS_FILE
echo "Available computers :"
for computer in ${available[@]}; do
    echo $computer
done

# Do it in the log directory
logDir="log"
rm -rf $logDir
mkdir $logDir
cd $logDir

# Function to gather the log files, depending on the number of slaves
# $1: number of slaves
function gather_log {
    local nbSlaves=$1
    # Verify that there is at least nbSlaves+1 computers
    local min_computers=$((nbSlaves + 1))
    echo ${#available[@]}
    if [ ${#available[@]} -lt $min_computers ]; then
        echo "There must be at least $min_computers computers"
        exit 1
    fi
    # deploy slaves
    for i in $(seq 0 $nbSlaves) ; do
        local c=${available[${i}]}
        scp "$login@$c:$remoteFolder$logFilename" "$c-$logFilename"
        echo "---------- Log from $c ----------" >> $logFilename
        echo >> $logFilename
        cat "$c-$logFilename" >> $logFilename
        echo >> $logFilename
    done
}

gather_log 3

# echo "Gathering logs from slaves in $logFilename"
# data_time=$(date '+%d/%m/%Y %H:%M:%S')
# echo $data_time > $logFilename
# for c in ${computers[@]}; do
#     scp "$login@$c:$remoteFolder$logFilename" "$c-$logFilename"
#     echo "---------- Log from $c ----------" >> $logFilename
#     echo >> $logFilename
#     cat "$c-$logFilename" >> $logFilename
#     echo >> $logFilename
# done
# echo "Adding the log from master to $logFilename"
# scp "$login@$master:$remoteFolder$logFilename" "$master-$logFilename"
# echo "---------- Log from master ($master) ----------\n" >> $logFilename
# cat "$master-$logFilename" >> $logFilename