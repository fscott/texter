# Texter - A program to search text files for a given term by various methods.

This program searches for words (either a user-selected word or a random word) within text files.

### Requirements:

1. Java 8 and [Apache Ant](https://ant.apache.org/bindownload.cgi) (to build).
2. A readable directory of files (ending in .txt) to search. The default is to use files in the `docs` directory.
3. (If using random words) A file of words (such as [the ten hundred most common words](http://splasho.com/upgoer5/phpspellcheck/dictionaries/1000.dicin)). The default is to use `res/words.txt`.
4. (If using Lucene) A readable directory to store an index. The default is to use `res/index`.

### Usage:

To build:

`ant dist`

To run:

`java -jar dist/Texter.jar`

### Methodology

This program consists of three search methods that are concrete class extending a Texter search interface:

1. StringMatchTexterImpl: (optionally) reads documents into memory and searches by matching characters of strings. 

2. RegexTexterImpl: (optionally) reads documents into memory and searches by a literal regex.

3. LuceneTexterImpl: creates a Lucene index of documents, searches for the presence of a term, and then gets word frequencies for each document that has at least one instance. 

Search is implemented as case insensitive in all cases.

Performance was initially evaluated with some provided text files over two million trials. Each trial used a random search term from the ten hundred most used words list.

Tests were run on a macbook pro with a 2.7 GHz Intel Core i5.

### Results

Averaging over three iterations of 2M trials using the provided three documents for each search method, Lucene search was the fastest. The Lucene method took slightly longer to get started but still less than a second to index the documents, which was a trivial cost compared to overall time saved over this number of trials. For one off searches with fewer than 100k trials, string search appeared to be faster.

As expected loading the documents into memory was faster than opening the files each time for String and Regex search.

See the results [here](https://github.com/fscott/texter/blob/master/results.pdf).

### Discussion

For greater than ~100k searches, Lucene search appears to give better performance. Only three documents were used for the initial tests. The Lucene index appears to have an advantage because it pretokenizes/processes the words and can efficiently retrieve results, compared to the Regex and String methods, which have to inspect nearly the entire document each trial.

Initial tests with larger text files (such as a dictionary or Moby Dick), performance slowed dramatically for all methods. Just adding Moby Dick to the set of original three files made processing 10k trials similar in performance to 2M trials without it. Smaller text files scaled better. Doubling the number of documents didn't affect performance, for example. Performance over 100k trials didn't double in time until 24 documents were indexed.

Perhaps, then, massively large documents should be chunked or if the possible number of search terms could be restricted so that frequencies wouldn't have to be computed/searched for huge lists of words.

Multi-threading is also a possibility. Adding .parallel() on the inner loops (looping over documents on a per trial basis) didn't affect performance much (these operations and normally pretty fast and the threading overhead appeared to wash out the advantages). Threading the search terms, however, appeared more promising. More care needs to be taken to ensure thread-safety (the output is also munged that way it's displayed now). This may especially help with the large documents.

If threading is enabled, more CPUs could increase throughput. For huge numbers of documents/content using the String and Regex searches with in-memory mode will eventually impact RAM.

If this program is running as a service (for example Elasticsearch and Solr use Lucene indices), you could load balance requests to multiple machines. Indices could be created on a master machine and replicated to slaves to scale at will. 


### TODO

- Implement a logging framework
- Explore creating a custom index that just consists of the frequencies, perhaps just a JSON file of maps 
of word frequencies for each document.
- Explore the effects of using .parallel()
- Allow some more interesting regexes?
- Should getHits be private?
- Unit Testing
- Better name for NonIndexedDocument (that doesn't confuse with Lucene Document.class)

