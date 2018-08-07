Search
Susan Liu and Sumit Sohani

1) Instructions for Use / User Interaction 
To run Index.scala, the user would compile Index.scala as normal, and then run it with three command line arguments. The first is a path to the corpus, the second is the name of the file to store the ID -> title map, and the third is a file to store the information about the words in the documents and their scores. 

To run Query.scala, the user would compile Query.scala like normal, and then run it with two command line arguments: namely, the names of the title and index files produced by Index. The user could also choose to include a --pagerank flag, which adds Hatrank, or a --smart flag, which (in our case) does the same thing as only running it with 2 args. 
If these file names are entered correctly, the user is then prompted to search for a query, after which the titles of the 10 most relevant pages appear. This REPL continues until the user types ":quit"

2) Overview of Design / Program Pieces
====
Index.scala
====
Upon creating a new Indexer object, we loop through all the documents to create a (title -> id) map. We ascribe the documents ids from 0 to n - 1 (where n is the size of the corpus) for the purposes of using Arrays, which are faster than HashMaps for simply storing/accessing data, since we know exactly how much we need. In this process, we also write to the title file.

Index.scala has three main methods: parse, hatRank, and writeToFiles.

- parse: Parse handles actually creating the TDFs of documents by placing them in an Array, whose indices are the document indices and whose elements are a Hashmap. Each Hashmap is specific to a document, and maps all the words in the document to its count. This Hashmap is normalized by dividing by the max score (updated as each word is looped through) at the end. The words are processed to handle both normal text as well as links, after which they are stemmed and lowercased. For each word, we also update the IDF as well as that particular document's W Array, which stores the scores that that particular documents ascribes to other documents. This is done by keeping track of the documents it links to (processed every time a link is found). 

- hatRank: Computes the hatRank score per document

-writeToFiles: Writes the actual index file by looping through the TDF array and then through the relevant hashtable. Incorporates the IDF score here, normalizing it if it already has not been normalized yet. 

3) Failed :( or Extra :) Features
We did not implement any extra features. In terms of failed features, our success with running BigWiki.xml has been spotty at best. We're definitely able to get through the first 50000 entries within a reasonable amount of time, but the process slows significantly after this. 

4) Bugs
As above, this may not run on BigWiki in a reasonable amount of time. 

5) Testing
====
INDEX
====
Tested on an xml file that we wrote called smallwiki.xml, containing 5 documents. 
We verified that the title file (smalltitle.txt) was output correctly, with the ID#Title format. Furthermore, we saw that our overriden IDs (rather than the ones given) had been used. 

Next, we verified that the output of the index file was correct: number of documents at the very top, followed by each document (containing, on separate lines, id, hatRank score, word + " " + TDF * IDF, and a blank line at the very end), and 2 lines at the end to indicate EOF. 

We then verified our HatRank values using Excel, and also checked to see that they summed to 1. 

We then used excel again to tabulate TDFs, IDFS, and their products (which is what actually gets printed) to verify that the numbers in the document were valid. 

The link to this excel document is https://docs.google.com/a/brown.edu/spreadsheets/d/1ojyxgNJgUNW3c6ivaQ1nvS6j00DQfBeW5EBwwqdfN70/edit?usp=sharing

We also analyzed the actual words for each document, verfiying that only the correct words were chosen (i.e. titles, the last part of a bar, both parts of a colon link, etc). We included special edge cases in our links, namely colon-bar links ([[Category:Blue|Bob]]), bar links with multiple words in them ([[a|these are multiple words]]) and multiple bar links ([[a|b|c]]). 

Overall, we also included an edge case of a document not having any words, which was successfully checked

The methods that we could easily unit test are included in the companion Tester object
====
QUERIER
====

====
DocIDPair
====
Really just a simple tuple, whose tests are included inside a companion object!

6) Collaboration
No one!