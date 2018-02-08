# UCI Undergraduate Research Program
## JinMyeong Lee, Computer Science & Software Engineering, Hanyang University, South Korea (jinious1111@hanyang.ac.kr)

2017.12.27 ~ 2018.2.15

***
#### Implementation
* How to compute the similarity between two sentences using Stanford parser.

* How to get the antecedent and the consequent in a sentence using Stanford parser.

***
#### Abstract
* To get the similarity, I use the Tree Edit Distance with tags level weight matrix.
* ZhangShasha algorithm is one of the Tree Edit Distance, and computes with Insert, Delete, Replace cost.
* I changed this cost with levels matrix.
* All tags can be classified to one of the 4 levels(Levels will be explaned at introduction section).
* And each level has own insert_delete weight, and replace cost.
* The antecedent can be parsed with SBAR tags. I found all antecedent has SBAR tag.
* The consequent can be parsed with excepting the SBAR tags and parsing VP.

***
#### Introduction
* First, I classified the all tags.
* 0 level(Clause level): S SBAR SBARQ SINV SQ
* 1 level(Phrase level): ADJP ADVP CONJP FRAG INTJ, etc.
* 2 level(Word level): CC CD DT EX FW IN JJ  JJR JJS LS MD, etc.
* 3 level(etc level): COMMA PERIOD ROOT.
* 

***
#### Additional Config file
* ./MyConfig/tags_levels.txt:<br/>
All tags should be in this file. All tags has own level.<br/>(level 0: Clause level, level 1: Phrase level, level 2: Word level, level 3: etc)<br/>Each string in this file means the level. If some tags which are not in this config file, you should add them.

* ./MyConfig/replace_weights.txt:<br/>
This matrix has replace costs which are used in ZhangShasha algorithm. (i, j) in matrix means i -> j level replace cost. And in the algorithm, that cost is multipled with float which is calculated with the string edit distance of the two tags. So the tags are more similar, then lower cost.

* ./MyConfig/insert_delete_weights.txt:<br/>
In this file, each double means each level's insert and delete weight.

input: Three config file pathes.<br/>
output: void.<br/>
function: read the config files.
```java
void readWeight(String levels, String replaceWeights, String insertDeleteWeights);
```
***

#### 1. Computing the Similarity.
To compute the similarity between two sentences, I divided the internal nodes and leaves. In the internal nodes, I used the ZhangShasha algorithm, one of the Tree Edit Distance algorithm. Simply, the ZhangShasha algorithm makes the subproblems dividing the parse tree using leftmosts and keyroots.

And for the leaves I used simple words comparing method. Because the internal nodes mean the structure of the sentence, there is no need to compare the sequence or order of the words in sentences. So just checking if there are the same words.
#### Internal Classes
* ZTree: used to calculate the tree edit distance
* Node: inner class of ZTree

#### Methods
###### Main Class
input: two Stanford parse trees.<br/>
return: String(float + "%")<br/>
function: The similarity of the internal nodes takes 90.00% and the similarity of the leaves takes 10.00%. compute separatly and add, then return.

```java
String getSimilarity(Tree tree1, Tree tree2);
```
input: tree, empty string.<br/>
output: one flatten long string which has tree structure.<br/>
function: Make one long parse tree string.

 ```java
 String parseString(Tree node, String parsed);
 ```

 ###### ZTree Class
 ```java
 double ZhangShasha(ZTree tree1, ZTree tree2);
```
