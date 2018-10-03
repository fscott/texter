# Texter - A program to search text files for a given term by various methods.

This program searches for words (either a user-selected word or a random word) within text files.

### Requirements:

1. Java 8 and Ant (to build).
2. A readable directory of files (ending in .txt) to search. The default is to use files in the `docs` directory.
3. (If using random words) A file of words (such as [the ten hundred most common words](http://splasho.com/upgoer5/phpspellcheck/dictionaries/1000.dicin)). The default is to use `res/words.txt`.
4. (If using Lucene) A readable directory to store an index. The default is to use `res/index`.

### Usage:

To build:

`ant dist`

To run:

`java -jar dist/Texter.jar`

### Discussion



### Results

See the results [here](https://github.com/fscott/texter/blob/master/results.pdf).

### TODO

- Implement a logging framework
- Explore the effects of using .parallel()
- Allow some more interesting regexes?
- Should getHits be private?
- Unit Testing
- Better name for NonIndexedDocument (that doesn't confuse with Lucene Document.class)

