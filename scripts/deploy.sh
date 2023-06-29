#!/bin/bash

source ./scripts/config.sh

# Function to clean the remote folder and
# copy necessary files in a new remote folder on a computer
# $1: number of slaves
# $2: computer
# $3: the file to split
function prepare {
    local nb_slaves=$1
    local c=$2
    local file=$3
    colorized_echo "${CYAN}" "---------------"
    colorized_echo "${CYAN}" "Apply to $c"
    colorized_echo "${CYAN}" "---------------"
    # All the commands to execute on the remote computer
    echo_command "ssh" "$login@$c" "rm -rf $remoteFolder;mkdir $remoteFolder"
    echo_command "scp" "-r" "$directory" "$login@$c:$remoteFolder$directory"
    echo_command "scp" "$buildFilename" "$AVAILABLE_COMPUTERS_FILE" "$login@$c:$remoteFolder"
    # Split the file
    # echo_command "scp" "-r" "${SCRIPT_FOLDER}" "$login@$c:${remoteFolder}${SCRIPT_FOLDER}"
    # echo_command "ssh" "$login@$c" "chmod +x ${remoteFolder}${SCRIPT_FOLDER}*.sh"
    # echo_command "ssh" "$login@$c" "cd $remoteFolder; ./${SCRIPT_FOLDER}split.sh $file $nb_slaves splits/ -f;"
    # echo_command "ssh" "$login@$c" "ls ${remoteFolder}splits/"
}

# Function to create the remote folder on a computer
# $2: computer
# $4: number of splits
# $3: the file to split
# $5: the output folder
function create_split {
    local c=$2
    local file=$3
    local nb_splits=$4
    local output_folder=$5
    echo_command "scp" "-r" "${SCRIPT_FOLDER}" "$login@$c:${remoteFolder}${SCRIPT_FOLDER}"
    echo_command "ssh" "$login@$c" "chmod +x ${remoteFolder}${SCRIPT_FOLDER}*.sh"
    echo_command "ssh" "$login@$c" "cd $remoteFolder; ./${SCRIPT_FOLDER}split.sh $file $nb_splits $output_folder -f;"
}

# Function to copy the splits from a computer to another
# $2: destination computer
# $3: source computer
function copy_split {
    local split_dest=$2
    local split_src=$3
    echo_command "scp" "-r" "$login@${split_src}:${remoteFolder}splits/" "$login@${split_dest}:${remoteFolder}"
    # echo_command "ssh" "$login@${split_dest}" "ls ${remoteFolder}splits/"
}

# Function to launch a slave, after killing all the java processes
# $1: number of slaves - 1 (index of the last slave)
# $2: computer
# $3: number of splits per slave
# $4 : the file to split
function launch_slave {
    local nb_slaves=$(($1 + 1))
    local c=$2
    local args="$nb_slaves ${@:3}"
    colorized_echo "${CYAN}" "--- Running slave $c ---"
    echo_command "ssh" "$login@$c" "pkill -u $login -f java"
    echo_command "ssh" "$login@$c" "cd $remoteFolder;ant $slaveCommand -Dargs-slave=\"$args\" > $logFilename" &
}

# Function to launch the master, after killing all the java processes
# $1: number of slaves (index of master)
# $2: computer
function launch_master {
    local nb_slaves=$1
    local c=$2
    local args="$nb_slaves ${@:3}"
    colorized_echo "${CYAN}" "--- Running master $c ---"
    echo_command "ssh" "$login@$c" "pkill -u $login -f java"
    echo_command "ssh" "$login@$c" "cd $remoteFolder; ant $masterCommand -Dargs-master=\"$args\" > $logFilename" &
}

# Function to run all the slaves and the master
# $1: number of slaves
# $2: the output prefix
# $3: number of splits per slave
# $4: the sleeping time in seconds between the slaves and master (optional)
function run_all {
    local nb_slaves=$1
    local output_prefix=$2
    apply_to_computers "$(($nb_slaves - 1))" "launch_slave" "$AVAILABLE_COMPUTERS_FILE" "$3" "$output_prefix"
    # if the 3rd argument is empty, sleep 20 seconds, otherwise sleep $3 seconds
    if [ -z "$4" ]; then
        local sleeping_time=20
    else
        local sleeping_time=$4
    fi
    colorized_echo "${CYAN}" "Sleeping $sleeping_time seconds"
    sleep $sleeping_time
    launch_master "$nb_slaves" "${available[$nb_slaves]}" "$AVAILABLE_COMPUTERS_FILE"
}

# Function to gather the log files, depending on the number of slaves
# $1: number of slaves
# $2: the prefix of the log file where to gather the logs
function gather_log {
    local nbSlaves=$1
    local gathered_log=$2$logFilename
    # Do it in the log directory
    pushd $LOG_FOLDER
    colorized_echo ${CYAN} '--- Gathering logs from slaves and master ---'
    data_time=$(date '+%d/%m/%Y %H:%M:%S')
    echo $data_time > "$gathered_log"
    # get the log from remote computers and add it to the log file
    for i in $(seq 0 $nbSlaves) ; do
        local c=${available[${i}]}
        echo -e "\n---------- Log from $c ----------\n" >> "$gathered_log"
        ssh "$login@$c" "cat $remoteFolder$logFilename" >> "$gathered_log"
    done
    # Go back to the previous directory
    popd
}

# Function to launch the experiment
function main {
    
    # local variables
    local slaves_range=(1 2 3 4 6 8 12 24)
    # local slaves_range=(24)
    local max_slaves=${slaves_range[-1]}
    
    local header="Number of slaves;Loading;Map 1;Shuffle 1;Reduce 1;Map 2;Shuffle 2;Reduce 2;Total 1;Total 2;Total"
    local tmp_results_file="results.csv"
    local sleep_before_log=50

    local filename=$(basename "$input_file")  # Extract the file name
    local filename_without_extension="${filename%%.*}" # Extract the filename without the extension
    local output_folder="splits/splits-$filename_without_extension-$max_slaves" # Folder to store the output files
    local output_prefix="$remoteFolder$output_folder/output_" # Output file

    # local variables
    echo "Login: $login"
    echo "Max slaves: $max_slaves"
    echo "Slaves range: ${slaves_range[*]}"
    echo "Filename: $filename"
    echo "Input file: $input_file"
    echo "Output folder: $output_folder"

    # get the available computers
    getAvailableComputers
    verify_nb_computers "$max_slaves"

    if [ $rebuild -eq 0 ]; then
        # prepare all the computers with the script folder
        apply_to_computers "$max_slaves" "prepare" "$input_file"
        # too slow :
        # create_split "$nb_slaves" "${available[${nb_slaves}]}" "$input_file" "3"
        # apply_to_computers "$(($nb_slaves - 1))" "copy_split" "${available[${nb_slaves}]}"
        # faster :
        apply_to_computers "$(($max_slaves - 1))" "create_split" "$input_file" "$max_slaves" "$output_folder"
    fi

    rm -rf $LOG_FOLDER
    mkdir $LOG_FOLDER
    echo "$header" > $RESULTS_FILE

    for nb_slaves in ${slaves_range[@]}; do
        if [ $((max_slaves % nb_slaves)) -ne 0 ]; then
            colorized_echo ${RED} "Error: max_slaves ($max_slaves) must be a multiple of nb_slaves ($nb_slaves)"
            exit 1
        fi
        local splits_per_slave=$(($max_slaves / $nb_slaves))
        local master=${available[${nb_slaves}]}
        colorized_echo ${CYAN} "Running with $nb_slaves slaves, master is $master"
        run_all "$nb_slaves" "$output_prefix" "$splits_per_slave" "10"
        sleep $sleep_before_log
        gather_log "$nb_slaves" "$nb_slaves-"
        ssh "$login@$master" "cat $remoteFolder$RESULTS_FILE" >> "$RESULTS_FILE"
        # wait before other ssh to not be banned
        sleep 60
    done
    
}

# Function to print the usage of the script
function usage {
    colorized_echo ${GREEN} "Usage: $0 [-l login] [-f file] [-c file] [-b] [-h]"
    echo "  -l login: the login to use to connect to the remote computers"
    echo "  -f file: the input file to split and process, if it is a number it will be used as the suffix for commoncrawl files"
    echo "  -c file: the file containing the list of available computers"
    echo "  -b: not copy the needed source code to the computers and not regenerate the splits (default: false)"
    echo "  -h: print this help"
    exit 1
}

input_file=DEFAULT_INPUT_FILE
rebuild=0

while getopts ":l:f:c:bh" opt; do
    case $opt in
        l)
            login=$OPTARG
            ;;
        f)
            if [[ $OPTARG =~ ^[0-9]+$ ]]; then
                if [ $OPTARG -lt 10 ]; then
                    input_file="${DEFAULT_PREFIX_INPUT_FILE}0$OPTARG$DEFAULT_SUFFIX_INPUT_FILE"
                else
                    input_file="${DEFAULT_PREFIX_INPUT_FILE}$OPTARG$DEFAULT_SUFFIX_INPUT_FILE"
                fi
            else
                input_file=$OPTARG
            fi
            ;;
        c)
            AVAILABLE_COMPUTERS_FILE=$OPTARG
            ;;
        b)
            rebuild=1
            ;;
        h)
            usage
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            usage
            ;;
    esac
done

main

# getAvailableComputers
# echo "Master is on ${available[24]}"
# run_all "24" "/tmp/echatelin-21/splits/splits-CC-MAIN-20230320083513-20230320113513-00001-24/output_" "1" "10"
# sleep 50
# gather_log 24 "24-"
# ssh "$login@${available[24]}" "cat $remoteFolder$RESULTS_FILE" >> "$RESULTS_FILE"
