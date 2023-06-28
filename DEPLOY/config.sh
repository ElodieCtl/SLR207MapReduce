# Configuration

login="echatelin-21"
remoteFolder="/tmp/$login/"
directory="src"
buildFilename="build.xml"
slaveCommand="run-slave"
masterCommand="run-master"
#computers=("tp-3b07-13" "tp-3b07-14" "tp-3b07-15")
#master="tp-3b07-12"
dataFilename="data/input.txt"
logFilename='log.txt'
AVAILABLE_COMPUTERS_FILE='computers.txt'
SCRIPT_FOLDER='DEPLOY/'
LOG_FOLDER='logs/'
RESULTS_FILE='results.csv'

# Default values for input file
DEFAULT_PREFIX_INPUT_FILE="/cal/commoncrawl/CC-MAIN-20230320083513-20230320113513-000"
DEFAULT_SUFFIX_INPUT_FILE=".warc.wet"
DEFAULT_INPUT_FILE="${DEFAULT_PREFIX_INPUT_FILE}73$DEFAULT_SUFFIX_INPUT_FILE"

# Color codes
BLACK=30
RED=31
GREEN=32
YELLOW=33
BLUE=34
MAGENTA=35
CYAN=36
WHITE=37

# Function to print a message in a color
# $1: color (number between 30 and 37)
# $2: message
function colorized_echo {
    echo -e "\e[${1}m${2}\e[0m"
}

# Function to echo the elements of the command and execute it
function echo_command {
    local command_array=("$@")  # Create a reference to the array specified by the argument
    echo "${command_array[*]}"  # Echo all elements of the array
    "${command_array[@]}"  # Echo each element separately
}

function getAvailableComputers {
    if [ ! -f $AVAILABLE_COMPUTERS_FILE ]; then
        colorized_echo ${YELLOW} "Computers file not found, creating it..."
        local computers_tmp="computers_tmp.txt"
        ssh "$login@ssh.enst.fr" "tp_up" > "$computers_tmp"
        shuf "$computers_tmp" > "$AVAILABLE_COMPUTERS_FILE"
        rm "$computers_tmp"
    fi
    # Read computers from file
    readarray -t available < $AVAILABLE_COMPUTERS_FILE
    local size=${#available[@]}
    local max_to_show=$(( $size < 5 ? $size : 5 ))
    echo "$size available computers (showing $max_to_show) :"
    for i in $(seq 0 $(( $max_to_show - 1 ))); do
        echo ${available[${i}]}
    done
}

# Verify that there is at least nbSlaves+1 computers
# $1: number of slaves
function verify_nb_computers {
    local nbSlaves=$1
    local min_computers=$((nbSlaves + 1))
    if [ ${#available[@]} -lt $min_computers ]; then
        colorized_echo $RED "There must be at least $min_computers available computers !"
        colorized_echo $RED "Please add some computers in the file $AVAILABLE_COMPUTERS_FILE."
        exit 1
    fi
}

# Function to apply a function to all the computers
# $1: number of computers - 1
# $2: the function to apply (the first argument of the function is the number of computers - 1, the second is the computer)
# $3...: the arguments of the function
function apply_to_computers {
    local max_computers=$1
    local function_to_apply=$2
    colorized_echo "${CYAN}" "Apply $function_to_apply to $(($max_computers + 1)) computers"
    shift 2
    for i in $(seq 0 $max_computers) ; do
        "$function_to_apply" "$max_computers" "${available[${i}]}" "$@"
    done
}
