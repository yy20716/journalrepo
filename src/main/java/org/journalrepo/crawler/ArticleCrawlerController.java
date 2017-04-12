package org.journalrepo.crawler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.sql2o.Sql2o;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ArticleCrawlerController {
	private static Logger logger = Logger.getLogger(ArticleCrawlerController.class.getName());

	private static String baseAddress = "media.daum.net";
	private static String articleListBaseAddress = "/api/service/news/list/breakingnews.jsonp";
	private static HTTPProxyConnection proxyConnection = new HTTPProxyConnection(baseAddress);
	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
	
	public static void main(String args[]) throws IOException{
		if (args.length > 0 && args[0].contains("clear")) {
			logger.info("Truncate all collected data in a database...");
			truncateTables();
			return;
		}
		
		if (args.length > 1 && (args[0].isEmpty() || args[1].isEmpty())) {
			logger.error("Usage: cr startdate enddate, e.g., cr 20151224 20151225");
			return;
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date startDate = null, endDate = null;
		try {
			startDate = formatter.parse(args[0]);
			endDate = formatter.parse(args[1]);
		} catch (ParseException e) {
			logger.error("It seems that startDate/endDate is not properly given: " + args[0] + " : " + args[1]);
			return;
		}

		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = start.getTime();
		String formattedDate = null;
		int page = 1;
		
		while (start.before(end)) {
			date = start.getTime();
			formattedDate = DateFormat.format(date);
			int expPageCount =  getExpPageCount(formattedDate);

			for (page = 1; page < expPageCount + 1; page += 1) {
				List<Future<Boolean>> resultList = new ArrayList<>();
				ArticleCrawler testcrawler = new ArticleCrawler(formattedDate, page);
				Future<Boolean> result = executor.submit(testcrawler);
				resultList.add(result);
			}

			start.add(Calendar.DATE, 1);
		}

		executor.shutdown();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutting down threads for article cralwers...");
				executor.shutdown();
			}
		});
	}

	private static int getExpPageCount(String formattedDate) {
		String addressComplete = articleListBaseAddress + "?regdate=" + formattedDate + "&page=1&newsType=title";
		String rawPageStr;
		try {
			rawPageStr = proxyConnection.connectPage(addressComplete);

			JsonParser parser = new JsonParser();
			JsonObject jsonObject = parser.parse(rawPageStr).getAsJsonObject();
			int countPerPage = jsonObject.get("countPerPage").getAsInt();
			int totalCount = jsonObject.get("totalCount").getAsInt();

			return totalCount / countPerPage;
		} catch (Exception e) {
			logger.error(e.getMessage(), e.getCause());
			return -1;
		}
	}

	private static void truncateTables() {
		Sql2o sql2o = new Sql2o("jdbc:mysql://localhost:3306/JournalRepo?characterEncoding=utf8", "journalrepo", "journal0306");
		org.sql2o.Connection dbcon = null;

		try {
			dbcon = sql2o.open();

			String delArticleBodySql = "DELETE FROM ArticleBody";
			String delArticleReporterSql = "DELETE FROM ArticleReporter";
			String delArticleSql = "DELETE FROM Article";
			String delReporterSql = "DELETE FROM Reporter";
			String delAgencySql = "DELETE FROM Agency";

			String alArticleSql = "ALTER TABLE Article AUTO_INCREMENT = 1";
			String alReporterSql = "ALTER TABLE Reporter AUTO_INCREMENT = 1";
			String alAgencySql = "ALTER TABLE Agency AUTO_INCREMENT = 1";

			dbcon.createQuery(delArticleReporterSql).executeUpdate();
			dbcon.createQuery(delArticleSql).executeUpdate();
			dbcon.createQuery(delArticleBodySql).executeUpdate();
			dbcon.createQuery(delReporterSql).executeUpdate();
			dbcon.createQuery(delAgencySql).executeUpdate();

			dbcon.createQuery(alArticleSql).executeUpdate();
			dbcon.createQuery(alReporterSql).executeUpdate();
			dbcon.createQuery(alAgencySql).executeUpdate();

			dbcon.close();
		} catch (Exception e) {

		}
	}
}
