# SLR207 - MapReduce

## Sequential counter

A HashMap, especially `HashMap<String, Integer>` allows us to associate a number to a unique word. However, when we sort the results, we need a list to guarantee the order of the elements. It is faster than a TreeMap.

Here are the results that I had on the different files :

| File                         | Time to count (ms) | Time to sort (ms) | Total time (ms) | 5 most frequent words            |
|------------------------------|--------------------|-------------------|-----------------|----------------------------------|
| forestier_mayotte            | 3                  | 1                 | 4               | de - bien - ou - forestier - des |
| deontologie_police_nationale | 10                 | 2                 | 12              | de - la - police - et - des      |
| domaine_public_fluvial       | 22                 | 4                 | 26              | de - le - du - la - et           |
| sante_publique               | 946                | 55                | 1001            | de - la - des - les - et         |
| common_crawl                 | 10996              | 4260              | 15256           | the - to - end - of - de         |

## MapReduce

Here are the results for the first part (MapReduce to count the occurences), on 3 workers and on files of the directory `/cal/commoncrawl` ending with 00070, 00071 and 00072.

* Load the splits : 3336ms
* Map : 15373ms
* Shuffle : 9696ms
* Reduce : 588ms

## DEPLOY

There are 3 bash scripts in the folder `DEPLOY` which can only be executed from the root folder :
- `config.sh` : all the variables needed, such as the paths and the names of the machines
- `deploy.sh` : clean and deploy the slaves and then master
- `log.sh` : gather the log files from the remote machines into one (but keeping the individual files)

## TODO

- Get rid of the system.exit(1) and terminate correctly
- Use SortedMap as soon as the shuffle step
- Compare efficiency
- Other files (see the linebreaks)

