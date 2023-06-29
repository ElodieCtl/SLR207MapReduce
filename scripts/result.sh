#!/bin/bash

source scripts/config.sh

# Read computers from file
getAvailableComputers

# Do it in the result directory
resDir="result"
resultFilename="result.txt"
rm -rf $resDir
mkdir $resDir
cd $resDir

# Function to gather the result files, depending on the number of slaves
# $1: number of slaves
function gather_result {
    local nbSlaves=$1
    # Verify that there is at least nbSlaves+1 computers
    local min_computers=$((nbSlaves + 1))
    if [ ${#available[@]} -lt $min_computers ]; then
        echo "There must be at least $min_computers computers"
        exit 1
    fi
    # gathering result of the slaves
    for i in $(seq 0 $((nbSlaves - 1))) ; do
        local c=${available[${i}]}
        scp "$login@$c:$remoteFolder\result-$i.txt" "$c-$resultFilename"
        echo "---------- Result from $c ----------" >> $resultFilename
        echo >> $resultFilename
        cat "$c-$resultFilename" >> $resultFilename
        echo >> $resultFilename
    done
}

gather_result $@

# nbSplits=${#computers[@]}
# maxIndex=$(($nbSplits-1))

# echo "Gathering logs from slaves in $resultFilename"
# data_time=$(date '+%d/%m/%Y %H:%M:%S')
# echo $data_time > $resultFilename
# for i in $(seq 0 $maxIndex) ; do
#     c=${computers[$i]}
#     scp "$login@$c:$remoteFolder\result-$i.txt" "$c-$resultFilename"
#     echo "---------- Log from $c ----------" >> $resultFilename
#     echo >> $resultFilename
#     cat "$c-$resultFilename" >> $resultFilename
#     echo >> $resultFilename
# done
# echo "Adding the log from master to $resultFilename"
# scp "$login@$master:$remoteFolder$resultFilename" "$master-$resultFilename"
# echo "---------- Log from master ($master) ----------\n" >> $resultFilename
# cat "$master-$resultFilename" >> $resultFilename