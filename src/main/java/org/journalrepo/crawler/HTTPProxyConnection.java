package org.journalrepo.crawler;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.sql2o.Sql2o;

// http://txt.proxyspy.net/proxy.txt
public class HTTPProxyConnection {
	static Logger logger = Logger.getLogger(HTTPProxyConnection.class.getName());
	private static String selProxyAddrSql = "SELECT idProxy, address, alive FROM HTTPProxy WHERE alive = :alive";

	private static int retryMaxCnt = 50;
	private static int socketTimeout = 10 * 1000;
	private static int connectionTimeout = 10 * 1000;
	private static int connectionRequestTimeout = 10 * 1000;

	private static Sql2o sql2o;

	private org.sql2o.Connection dbcon;
	private List<HTTPProxy> proxyList = null;
	private String baseAddress = null;
	private int retryCnt = 0;

	static{
		sql2o = new Sql2o("jdbc:mysql://localhost:3306/JournalRepo?characterEncoding=utf8", "journalrepo", "journal0306");
	}

	public HTTPProxyConnection(String baseAddress) {
		this.baseAddress = baseAddress;
		dbcon = sql2o.open();
		proxyList = dbcon.createQuery(selProxyAddrSql)
				.addParameter("alive", 1)
				.executeAndFetch(HTTPProxy.class);
	}

	protected void finalize() throws Throwable {
		try {
			dbcon.close();
		} finally {
			super.finalize();
		}
	}

	public String connectPage(String address) throws ClientProtocolException, IOException {
		retryCnt = 0;

		while (retryCnt < retryMaxCnt) {
			CloseableHttpClient httpclient = HttpClients.custom()
					.setUserAgent("Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)")
					.build();

			try {
				HTTPProxy randomProxy = proxyList.get(new Random().nextInt(proxyList.size()));
				HttpHost target = new HttpHost(baseAddress, 80, "http");
				HttpHost proxy = new HttpHost(randomProxy.address, 80, "http");

				RequestConfig config = RequestConfig.custom()
						.setProxy(proxy)
						.setSocketTimeout(socketTimeout)
						.setConnectTimeout(connectionTimeout)
						.setConnectionRequestTimeout(connectionRequestTimeout)
						.build();

				HttpGet request = new HttpGet(address);
				request.setConfig(config);

				// logger.info("Executing request " + request.getRequestLine() + " to " + target + " via " + proxy);
				CloseableHttpResponse response = httpclient.execute(target, request);
				StatusLine sl = response.getStatusLine();
				int slCode = sl.getStatusCode();
				if (slCode != 404 && slCode >= 300) {
					retryConn();
					logger.error("Status Code" + slCode + ": Retry count: " + retryCnt);
					continue;
				}

				try {
					retryCnt = 0;
					return EntityUtils.toString(response.getEntity());
				} finally {
					response.close();
				}
			} catch (Exception e) { 
				retryConn();
				logger.error("Connection Error: " + e.getMessage() + " Retry count: " + retryCnt);
			} finally {
				httpclient.close();
			}
		}

		logger.error("HTTPProxyConnection is terminated because it exceeds its maximum retry number for reconnections.");
		return null;
	}

	private void retryConn() {
		try {
			retryCnt += 1;
			long ransec = (long)(Math.random() * 2000);					
			Thread.sleep(ransec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}