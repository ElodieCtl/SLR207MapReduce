#!/bin/bash

source DEPLOY/config.sh

# If you want to change the available computers, you have to change the file computers.txt
# echo 'tp-3b07-13' > 'computers.txt'
# echo 'tp-3b07-14' >> 'computers.txt'
# echo 'tp-3b07-15' >> 'computers.txt'
# echo 'tp-3b07-12' >> 'computers.txt'

# Read computers from file
readarray -t available < $AVAILABLE_COMPUTERS_FILE
echo "Available computers :"
for computer in ${available[@]}; do
    echo $computer
done

# Function to kill java processes, clean the remote folder and
# copy necessary files in a new remote folder on a computer
# $1: computer
function prepare_computer {
    local c=$1
    echo -e "---------------\nClean and copy on $c"
    # kill all java processes
    local command0=("ssh" "$login@$c" "lsof -ti | xargs kill -9")
    # remove remote folder and create it again
    local command1=("ssh" "$login@$c" "rm -rf $remoteFolder;mkdir $remoteFolder")
    local command2=("scp" "-r" "$directory" "$login@$c:$remoteFolder$directory")
    local command3=("scp" "$buildFilename" "$AVAILABLE_COMPUTERS_FILE" "$login@$c:$remoteFolder")
    echo ${command0[*]}
    "${command0[@]}"
    echo ${command1[*]}
    "${command1[@]}"
    echo ${command2[*]}
    "${command2[@]}"
    echo ${command3[*]}
    "${command3[@]}"
}

for computer in ${available[@]}; do
    prepare_computer $computer
done

# Function to deploy the slaves and the master, depending on the number of slaves
# $1: number of slaves
function deploy {
    local nbSlaves=$1
    # Verify that there is at least nbSlaves+1 computers
    local min_computers=$((nbSlaves + 1))
    if [ ${#available[@]} -lt $min_computers ]; then
        echo "There must be at least $min_computers computers"
        exit 1
    fi
    local args="$AVAILABLE_COMPUTERS_FILE $nbSlaves"
    echo -e "---------------\nDeploying $nbSlaves slaves and 1 master"
    # deploy slaves
    for i in $(seq 0 $((nbSlaves-1))) ; do
        local c=${available[${i}]}
        local command=("ssh" "$login@$c" "cd $remoteFolder;ant $slaveCommand -Dargs=\"$args\" > $logFilename")
        echo ${command[*]}
        "${command[@]}" &
    done
    # deploy master
    local master=${available[$((nbSlaves))]}
    local command=("ssh" "$login@$master" "cd $remoteFolder;ant $masterCommand -Dargs=\"$args\" > $logFilename")
    echo ${command[*]}
    "${command[@]}" &
}

# Deploy 3 slaves and 1 master
deploy 3

# for c in ${computers[@]}; do
#     echo -e "---------------\nDeploying slave on $c"
#     command=("ssh" "$login@$c" "cd $remoteFolder;ant $slaveCommand > $logFilename")
#     echo ${command[*]}
#     "${command[@]}" &
# done

# echo -e "---------------\nDeploying master on $master"
# command=("ssh" "$login@$master" "cd $remoteFolder;ant $masterCommand > $logFilename")
# echo ${command[*]}
# "${command[@]}" &