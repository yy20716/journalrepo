# JournalRepo
A simple program that crawls news articles from Daum and stores in local databases.

# QuickStart
Running `mvn package` does a compile and creates the target directory, including a jar as follows.
```
yy20716:~/workspace/journalrepo$ mvn clean 
yy20716:~/workspace/journalrepo$ mvn package > /dev/null
```
Execute the jar file for crawling articles. Please note that this jar requires two parameters: startdata and enddata using yyyymmdd format. For example, you can crawl articles published between Jan 1, 2015 and Jan 1, 2016 as follows.
```
yy20716:~/workspace/journalrepo$ java -jar target/journalrepo.jar 20150101 20160101
...
```

# Contributors
[HyeongSik Kim](https://www.linkedin.com/in/hskim0)
