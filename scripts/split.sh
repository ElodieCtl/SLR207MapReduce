#!/bin/bash

source ./scripts/config.sh

function echo_split {
  colorized_echo "${BLUE}" "[split.sh] $@${RESET}"
}

# function to split a file into multiple files
# $1: input file
# $2: number of output files
# $3: folder where the output files will be stored.
#     If the folder already exists, then the script will ask for confirmation before overwriting it.
#     If the folder doesn't end with a slash (/), then the script will add one at the end.
# $4: -f to force overwriting the output folder if it already exists
function split_file() {
  local f="$1"  # Input file
  local N="$2"  # Number of output files
  echo_split "Splitting the file $f into $N files."

  local folder="$3"  # Parent folder where the output files will be stored
  # Check if the variable path ends with a slash
  if [[ ${folder} != */ ]]; then
    # Add a slash to the end of the variable
    folder="${folder}/"
  fi

  #local suffix=".${f##*.}"  # Extract the file extension
  local suffix=".txt"

  # local filename=$(basename "$f")  # Extract the file name
  # local filename_without_extension="${filename%%.*}" # Extract the filename without the extension
  # local folder="${path}splits-$filename_without_extension-$N" # Folder to store the output files
  echo_split "Output folder: $folder"

  # Create a folder to store the output files
  # and delete the folder if it already exists
  if [ -d "$folder" ]; then
    # if the user didn't pass -f as the 3rd argument, then ask for confirmation before overwriting the folder
    if [ "$4" != "-f" ]; then
      # ask for confirmation before overwriting the folder
      read -p "The folder $folder already exists. Do you want to overwrite it? (y/n) " answer
      # if the user's answer isn't y or Y, then exit the script
      if [[ ! "$answer" =~ ^[yY]$ ]]; then
        echo_split "Exiting the script."
        return 0
      fi
    fi
    echo_split "Deleting the folder $folder."
    rm -rf "$folder"
  fi
  mkdir -p "$folder"

  # Count the total number of lines in the input file
  local M=$(wc -l < "$f")
  echo_split "Total number of lines in the file: $M"

  # Check if N is less than M
  if (( M < N )); then
    echo_split "Error: N should be less than or equal to M"
    return 1
  fi

  # Compute the number of lines per file

  # if M % N == 0 then there will be M/N lines per file.
  # if M % N <= N/2 then there will be M/N lines per file, except for the last file, which will be larger.
  # if M % N > N/2 then there will be M/N + 1 lines per file, except for the last file, which will be shorter.

  local lines_per_file=$(( M / N ))
  local remainder=$(( M % N ))  # Number of remaining lines for the last file
  if (( remainder > N / 2 )); then
    local lines_per_file=$(( lines_per_file + 1 ))
    local last_file_lines=$(( M % lines_per_file ))
    local remainder=0
  else
    local last_file_lines=$(( lines_per_file + remainder ))
  fi
  echo_split "Number of lines per file: $lines_per_file"
  echo_split "Number of remaining lines for the last file: $last_file_lines"

  # Split the file into N output files
  # -d to name the files as output-xx with xx in decimal starting from 00
  # -l to specify the number of lines per file
  # --additional-suffix to add the file extension, here .txt
  local output_prefix="${folder}output_"
  split -d -l "$lines_per_file" "$f" "$output_prefix" --additional-suffix="$suffix"

  # Rename the output files

  # Move the remaining lines to the last file
  if (( remainder > 0 )); then
    local last_file="${output_prefix}$(printf "%02d" "$((N - 1))")$suffix"  # Format the last file name
    local remainder_file="${output_prefix}$(printf "%02d" "$N")$suffix" # Format the remainder file name
    echo_split "Moving the remaining $remainder lines to the last file: $last_file"
    cat "$remainder_file" >> "$last_file"
    rm "$remainder_file"
  fi

  echo_split "File split completed successfully."
}

# split the file
split_file $@
