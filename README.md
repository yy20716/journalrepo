# JournalRepo
A simple crawler that automatically collects news articles from Daum news service (http://media.daum.net) and stores them in local databases. This crawler employs [JSoup](https://jsoup.org) for parsing crawled html files and thread-pooling techinques to parallize the collection process of news articles. In addition, the crawler supports an automatic extraction of the names of publishers and reporters, so that preliminary statistics on articles can be collected as well.

Please note that the automatic extraction feature is still in the preliminary stage, thus sometimes extracted names contain irrelevant strings or simply it is not working. We are currently working on that feature to improve the rate of the extractions.

# QuickStart
1. Running `mvn package` does a compile and creates the target directory, including a jar as follows.
```
yy20716:~/workspace/journalrepo$ mvn clean 
yy20716:~/workspace/journalrepo$ mvn package > /dev/null
```
2. This program uses mysql database to store crawled articles. Install *mysqld* first and generate tables by loading *JournalRepo.sql* in this repository first. 
```
mysql -u username -p database_name < JournalRepo.sql
```
*username* is hardcoded as *journalrepo* and *database_name* is JournalRepo in the current version of codes.

3. Execute the jar file for crawling articles. Please note that this jar requires two parameters: startdata and enddata using yyyymmdd format. For example, you can crawl articles published between Jan 1, 2015 and Jan 1, 2016 as follows.
```
yy20716:~/workspace/journalrepo$ java -jar target/journalrepo.jar 20150101 20160101
...
```
- Crawled articles are stored in the relation *Article* and *ArticleBody*.
- The relation *Agency* stores a list of agencies or publisher names extracted from articles. 
- Similarly, the relation *Reporter* stores the list of reporter names extracted in articles.

TODO. details will be added soon.

# Configuration
- Account information for connecting mysql database instance is currently hardcoded in ArticleCrawler.java (Line 46). 
- The number of cralwer instances can be adjusted by modifying the parameter of the thread number in ArticleCralwer.java (Line 26).
- This crawler uses http proxies to avoid automated blocking from Daum web services. The current codes randomly select a proxy server from the relation *HTTPProxy* for crawling each article. As a demonstration purpose, we add an example http proxy address in JournalRepo.sql but sometimes the proxy server is not available. In such a case, please add entries of http proxies by referring to any proxy list websites such as [Free IP:port proxy lists](http://proxylist.hidemyass.com).

# Contributors
[HyeongSik Kim](https://www.linkedin.com/in/hskim0)
