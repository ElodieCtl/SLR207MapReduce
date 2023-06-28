# SLR207 - Project

## How to use it ?

If you have `ant`, you can use `ant help` at the root of the project directory to see the options.
Otherwise, you can simply compile the `src` directory and then, run one of three main programs :
```bash
mkdir build
javac *.java -d build
java src.SequentialCounter
java src.master.Master
java src.slave.Slave
```
The two last programs will ask you to provide arguments.

If you want to run the experiment to deploy the map reduce on the school computers, you can use `ant`, `ant all` or `ant deploy`. This will launch the script `DEPLOY/deploy.sh` without any arguments. If you want to pass arguments, please launch directly the script as follow `./DEPLOY/deploy.sh arguments...`. _Make sure that when you run a script from the `DEPLOY` directory, you do it from the root directory of the project_ for the scripts to be able to execute or read other scripts.

If you want to custome the deploy configuration, you can either pass arguments when executing the script (see `./DEPLOY/deploy.sh -h` to list all the possible arguments) or change permanently the variables in `config.sh` or in another script.