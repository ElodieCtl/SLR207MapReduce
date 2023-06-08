#!/bin/bash

source DEPLOY/config.sh

# Deploy the slaves

for c in ${computers[@]}; do
  echo "Deploying slave on $c"
  # kill all java processes
  command0=("ssh" "$login@$c" "lsof -ti | xargs kill -9")
  # remove remote folder and create it again
  command1=("ssh" "$login@$c" "rm -rf $remoteFolder;mkdir $remoteFolder")
  command2=("scp" "-r" "$directory" "$login@$c:$remoteFolder$directory")
  command3=("scp" "$buildFilename" "$login@$c:$remoteFolder$buildFilename")
  # compile and run
  command4=("ssh" "$login@$c" "cd $remoteFolder;ant $slaveCommand > $logFilename")
  echo ${command0[*]}
  "${command0[@]}"
  echo ${command1[*]}
  "${command1[@]}"
  echo ${command2[*]}
  "${command2[@]}"
  echo ${command3[*]}
  "${command3[@]}"
  echo ${command4[*]}
  "${command4[@]}" &
done

# Deploy the master

echo "Deploying master on $master"
# kill all java processes
command0=("ssh" "$login@$master" "lsof -ti | xargs kill -9")
# remove remote folder and create it again
command1=("ssh" "$login@$master" "rm -rf $remoteFolder;mkdir $remoteFolder")
command2=("scp" "-r" "$directory" "$login@$master:$remoteFolder$directory")
command3=("scp" "$buildFilename" "$login@$master:$remoteFolder$buildFilename")
# don't forget to copy the data file
#command5=("scp" "../$dataFilename" "$login@$master:$remoteFolder$dataFilename")
# compile and run
command4=("ssh" "$login@$master" "cd $remoteFolder;ant $masterCommand > $logFilename")
echo ${command0[*]}
"${command0[@]}"
echo ${command1[*]}
"${command1[@]}"
echo ${command2[*]}
"${command2[@]}"
echo ${command3[*]}
"${command3[@]}"
#echo ${command5[*]}
#"${command5[@]}"
echo ${command4[*]}
"${command4[@]}" &
