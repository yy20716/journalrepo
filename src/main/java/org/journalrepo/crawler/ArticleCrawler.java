package org.journalrepo.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.apache.log4j.Logger;
import org.sql2o.Sql2o;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ArticleCrawler implements Callable<Boolean> {
	static Logger logger = Logger.getLogger(ArticleCrawler.class.getName());

	private String crawlDate = null;
	private int crawlPage = 0;
	private HTTPProxyConnection proxyConnection;

	private static String baseAddress = "media.daum.net";
	private static String articleBaseAddress = "/v";
	private static String articleListBaseAddress = "/api/service/news/list/breakingnews.jsonp";	
	private static String[] reporterIdStr = {" 인턴기자 ", " 기자 ", " 특파원 "};

	private static String insReporterSql =  "INSERT INTO Reporter (idAgency, name, emailAddress) values (:idAgency, :name, :emailAddress)";
	private static String insArtRepSql =  "INSERT INTO ArticleReporter (idArticle, idReporter) values (:idArticle, :idReporter)";
	private static String insArticleSql =  "INSERT INTO Article (idAgency, idWeb, title) values (:idAgency, :idWeb, :title)";
	private static String insArticleBodySql =  "INSERT INTO ArticleBody (idArticle, idAgency, idWeb, title, body) values (:idArticle, :idAgency, :idWeb, :title, :body)";
	private static String insAgencySql = "INSERT INTO Agency (engName, korName) values (:engName, :korName)";

	private static String selAgencySql = "SELECT idAgency FROM Agency WHERE engName = :engName and korName = :korName";
	private static String selReporterSql1 = "SELECT idReporter FROM Reporter WHERE idAgency = :idAgency and name = :name and emailAddress = :emailAddress";		

	private static Sql2o sql2o;
	private org.sql2o.Connection dbcon;
	static{
		sql2o = new Sql2o("jdbc:mysql://localhost:3306/JournalRepo?characterEncoding=utf8", "journalrepo", "journal0306");
	}

	public ArticleCrawler(String inputDate, int inputPage) {
		this.crawlDate = inputDate;
		this.crawlPage = inputPage;
	}

	protected void finalize() throws Throwable {
		try {
			dbcon.close();
			logger.info("An article crawler is finalized - date: " + this.crawlDate + " page: " + this.crawlPage);
		} finally {
			super.finalize();
		}
	}


	public void extractEntitiesFromArticle(String docBodyText, List<String> repoNameList, List<String> repoAddrList) throws IOException {		
		// 1. find email addresses of reporters in an article
		// Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(docBodyText);
		Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@").matcher(docBodyText);
		while (m.find()) {
			repoAddrList.add(m.group());
		}

		// 2. find names of reporters in an article
		int idPos = 0;
		for (String idStr: reporterIdStr) {			
			idPos = docBodyText.indexOf(idStr);

			if (idPos > 0) {
				String reporterName = docBodyText.substring(idPos-3, idPos).trim();

				if (repoNameList.contains(reporterName))
					repoNameList.remove(reporterName);

				repoNameList.add(reporterName);	
			}

			while (idPos >= 0) {
				idPos = docBodyText.indexOf(idStr, idPos + 1);

				if (idPos > 0) {
					String reporterName = docBodyText.substring(idPos-3, idPos).trim();

					if (repoNameList.contains(reporterName))
						repoNameList.remove(reporterName);

					repoNameList.add(reporterName);	
				}
			}
		}
	}

	private void checkReporterExist(Long idAgency, Long idArticle, List<String> repoNameList, List<String> repoAddrList) {

		int repoInd = 0;
		int minInd = 0;
		try {
			if (repoNameList.size() > 0 && repoAddrList.size() > 0) {
				minInd = Math.min(repoAddrList.size(), repoNameList.size());
				for (repoInd = 0; repoInd < minInd; repoInd++) {
					String repoName = repoNameList.get(repoInd);				
					String repoAddr = repoAddrList.get(repoInd);

					// Check whether we already have a record for a given reporter information.
					List<Long> reporterIds = dbcon.createQuery(selReporterSql1)
							.addParameter("idAgency", idAgency)
							.addParameter("name", repoName)
							.addParameter("emailAddress", repoAddr)
							.executeAndFetch(Long.class);				

					if (reporterIds.size() == 0) {
						Object insReporterId = dbcon.createQuery(insReporterSql, true)
								.addParameter("idAgency", idAgency)
								.addParameter("name", repoName)
								.addParameter("emailAddress", repoAddr)
								.executeUpdate()
								.getKey();

						reporterIds.add((Long) insReporterId);
					}

					dbcon.createQuery(insArtRepSql, true)
					.addParameter("idArticle", idArticle)
					.addParameter("idReporter", reporterIds.get(0))
					.executeUpdate();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private boolean visitEachArticle(JsonObject eachNewsObj) throws IOException {

		String idWeb = eachNewsObj.get("newsId").getAsString();
		String cpKorName = eachNewsObj.get("cpKorName").getAsString();
		String cpEngName = eachNewsObj.get("cpEngName").getAsString();
		String title = eachNewsObj.get("title").getAsString();

		String addressComplete = articleBaseAddress + "/" + idWeb;
		List<String> repoNameList = new ArrayList<String>();
		List<String> repoAddrList = new ArrayList<String>();

		logger.info("retrieving an article : " + addressComplete);
		
		String rawPageStr = proxyConnection.connectPage(addressComplete);
		if (rawPageStr == null) {
			logger.error("Cannot connect to news page : " + addressComplete);
			return false;
		}

		Document doc = Jsoup.parse(rawPageStr);
		Element docBody = doc.body();

		String cand1 = docBody.getElementsByClass("news_view").text();
		String cand2 = docBody.getElementsByClass("news_wrap").text();
		String docBodyText = null;

		if (cand1.length() > 0) 
			docBodyText = cand1;
		else if (cand2.length() > 0)
			docBodyText = cand2;

		if (docBodyText == null || docBodyText.isEmpty()) {
			logger.error("Article is empty? - Title: " + title + " Article Body: " + docBody.text());

			return false;
		}

		extractEntitiesFromArticle(docBodyText, repoNameList, repoAddrList);

		// 1. See whether a given agency exists in the table or not. 
		List<Long> agencyIds = dbcon.createQuery(selAgencySql)
				.addParameter("engName", cpEngName)
				.addParameter("korName", cpKorName)
				.executeAndFetch(Long.class);

		if (agencyIds.size() == 0) {
			Object insAgencyId = dbcon.createQuery(insAgencySql, true)
					.addParameter("engName", cpEngName)
					.addParameter("korName", cpKorName)
					.executeUpdate()
					.getKey();

			agencyIds.add((Long)insAgencyId);
		}

		// 2.
		Object insArticleId = dbcon.createQuery(insArticleSql, true)
				.addParameter("idAgency", agencyIds.get(0))
				.addParameter("idWeb", idWeb)
				.addParameter("title", title)
				.executeUpdate()
				.getKey();		    

		// 3.
		dbcon.createQuery(insArticleBodySql)
		.addParameter("idArticle", (Long)insArticleId)
		.addParameter("idAgency", agencyIds.get(0))
		.addParameter("idWeb", idWeb)
		.addParameter("title", title)
		.addParameter("body", docBodyText)
		.executeUpdate();

		checkReporterExist(agencyIds.get(0), (Long)insArticleId, repoNameList, repoAddrList);		

		return true;
	}

	public Boolean retrieveArticleList() throws IOException {
		String addressComplete = articleListBaseAddress + "?regdate=" + crawlDate + "&page=" + crawlPage + "&newsType=title";
		String docBodyText = proxyConnection.connectPage(addressComplete);

		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(docBodyText).getAsJsonObject();
		Boolean hasNext = jsonObject.get("hasNext").getAsBoolean();

		if (hasNext != true)
			return false;

		JsonArray simpleNews = jsonObject.get("simpleNews").getAsJsonArray();
		for (JsonElement eachNews : simpleNews) {
			JsonObject eachNewsObj = eachNews.getAsJsonObject();

			if (visitEachArticle(eachNewsObj) != true)
				continue;
		}

		return true;
	}

	@Override
	public Boolean call() throws Exception {
		try {
			dbcon = sql2o.open();			
			proxyConnection = new HTTPProxyConnection(baseAddress);
			logger.info("An article crawler is initiated - date: " + this.crawlDate + " page: " + this.crawlPage);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} 

		return retrieveArticleList();
	}
}
