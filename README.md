# JournalRepo
A simple crawler that automatically collects news articles from Daum news service (http://media.daum.net) and stores them in local databases.

# QuickStart
1. Running `mvn package` does a compile and creates the target directory, including a jar as follows.
```
yy20716:~/workspace/journalrepo$ mvn clean 
yy20716:~/workspace/journalrepo$ mvn package > /dev/null
```
2. This program uses mysql database to store crawled articles. Please generate tables by loading JournalRepo.sql in this repository before running the jar file. Account information for connecting mysql database instance can be currently hardcoded in ArticleCrawler.java (Line 46). The number of cralwer instances can be adjusted by modifying the parameter of the thread number in ArticleCralwer.java (Line 26). 

3. Execute the jar file for crawling articles. Please note that this jar requires two parameters: startdata and enddata using yyyymmdd format. For example, you can crawl articles published between Jan 1, 2015 and Jan 1, 2016 as follows.
```
yy20716:~/workspace/journalrepo$ java -jar target/journalrepo.jar 20150101 20160101
...
Crawled articles are stored in the table Article and ArticleBody (TODO. details will be added soon).
```

# Contributors
[HyeongSik Kim](https://www.linkedin.com/in/hskim0)
