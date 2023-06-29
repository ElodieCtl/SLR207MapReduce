# SLR207 - Project

Author : Elodie Chatelin.

## Prerequisites

The program is designed to run with Java 8 at minimum. It is better to have ant but you can manually run the scripts.

You should have access to the machines of Télécom Paris with some login (referenced as `login` in `scripts/config.sh`).

## Organisation of the project

```bash
./
│   .gitignore
│   build.xml
│   README.md
│
├───scripts
├───report
└───src
    ├───master
    └───slave
```

* In `scripts`, you can find all the bash scripts and data to launch the programs.
* In `report`, you can find the report, the plots and the data from the experiment in an Excel file.
* In `src`, you can find all the Java code. Directly in this folder, you can find all the generic codes used by slaves and master and the sequential version. In the subfolders `slave` and `master`, there are the main programs for respectively the slaves and the masters and the threads to exchange data between the machines. For more detailed explanations, see the [report](./report/Report.md).

## How to use it ?

If you have `ant`, you can use `ant help` at the root of the project directory to see the options.
Otherwise, you can simply compile the `src` directory and then, run one of three main programs :
`src.SequentialCounter`, `src.master.Master` or `src.slave.Slave`.

The two last programs will ask you to provide arguments.

If you want to run the experiment to deploy the map reduce on the school computers, you can use `ant`, `ant all` or `ant deploy`. This will launch the script `scripts/deploy.sh` without any arguments. If you want to pass arguments, please launch directly the script as follow `./scripts/deploy.sh arguments...`. _Make sure that when you run a script from the `scripts` directory, you do it from the root directory of the project_ for the scripts to be able to execute or read other scripts.

If you want to custome the deploy configuration, you can either pass arguments when executing the script (see `./scripts/deploy.sh -h` to list all the possible arguments) or change permanently the variables in `config.sh` or in another script.