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

## 2nd MAP REDUCE

1. Map : transform the maps with words as keys and their occurences as values into a new map where the keys are the occurences and the values are the list of words with this occurence.
2. Shuffle :
    - each of the slaves sends to master its minimum and maximum occurence
    - after collecting all the responses, the master send the global minimum _a_ and maximum _b_
    - given the number of slaves _n_, the slave _m_ is responsible for the _m_-th range of _(b-a)/n_ elements between _a_ and _b_
    - each slave send their entries to the responsible machine
3. Reduce : sort the values received and reduce them (concatenate lists for the same occurence)

## TODO

- Get rid of the system.exit(1) and terminate correctly
- Use SortedMap as soon as the shuffle step
- Compare efficiency
- Other files (see the linebreaks)

