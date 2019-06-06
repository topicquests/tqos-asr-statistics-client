/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IEnvironment;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class StatisticsHttpClient implements IStatisticsClient {
	private IEnvironment environment;
	private final String 
		SERVER_URL,
		CLIENT_ID,
		//verbs from IStatServerModel
		GET_STATS_VERB 		= "getStats",
		GET_KEY_VERB		= "getKey",
		ADD_TO_FIELD_VERB 	= "addToKey",
		//json fields from IStatServerModel
		VERB				= "verb",
		FIELD				= "field", //this is the short name of a key being counted
		ERROR				= "error", // used for error messages
		CARGO				= "cargo"; //return object a JSON blob of stats or key value

	/**
	 * 
	 */
	public StatisticsHttpClient(IEnvironment env) {
		environment = env;
		String urx = (String)environment.getProperties().get("StatServerURl");
		String port = (String)environment.getProperties().get("StatServerPort");
		CLIENT_ID = (String)environment.getProperties().get("StatServerClientId");
		SERVER_URL = "http://"+urx+":"+port+"/";
	}
	
	/**
	 * Increment a counter for <code>key</code>
	 * @param key
	 * @return
	 */
	public IResult addToKey(String key) {
		IResult result = new ResultPojo();
		//build query
		StringBuilder buf = new StringBuilder();
		buf.append("{\"verb\":\"addToKey\","); // the verb
		buf.append("\"field\":\""+key+"\","); // the field
		buf.append("\"clientId\":\""+CLIENT_ID+"\"}");
		String query = buf.toString();
		System.out.println("QUERY "+query);
		try {
			query = URLEncoder.encode(query, "UTF-8");
			getQuery(SERVER_URL+query, result);
		} catch (Exception e) {
			String x = e.getMessage()+" : "+buf.toString();
			environment.logError(x, e);
			result.addErrorString(x);
		}
		return result;
	}
	
	/**
	 * Get the entire statistics collection
	 * @return
	 */
	public IResult getStatistics() {
		IResult result = new ResultPojo();
		//build query
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"verb\":\"getStats\","); // the verb
		buf.append("\"clientId\":\""+CLIENT_ID+"\"}");
		String query = buf.toString();
		try {
			query = URLEncoder.encode(query, "UTF-8");
			getQuery(query, result);
		} catch (Exception e) {
			String x = e.getMessage()+" : "+buf.toString();
			environment.logError(x, e);
			result.addErrorString(x);
		}
		return result;		
	}
	
	public IResult getValueOfKey(String key) {
		IResult result = new ResultPojo();
		//build query
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"verb\":\"getKey\","); // the verb
		buf.append("\"field\":\""+key+"\","); // the field
		buf.append("\"clientId\":\""+CLIENT_ID+"\"}");
		String query = buf.toString();
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (Exception e) {
			String x = e.getMessage()+" : "+buf.toString();
			environment.logError(x, e);
			result.addErrorString(x);
		}
		return result;	
	}
	
	
	
	
	
	/**
	 * Simple HTTP client with a long timeout
	 * @param query
	 * @param result
	 */
	void getQuery(String query, IResult result) {
		
		BufferedReader rd = null;
		HttpURLConnection con = null;

		try {
			URL urx = new URL(query);
			con = (HttpURLConnection) urx.openConnection();
			con.setReadTimeout(500000); //29 seconds for 1m words - leave lots of time
			con.setRequestMethod("GET");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
			rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuilder buf = new StringBuilder();

			String line;
			while ((line = rd.readLine()) != null) {
				buf.append(line + '\n');
			}

			result.setResultObject(buf.toString());
		} catch (Exception var18) {
			var18.printStackTrace();
			result.addErrorString(var18.getMessage());
		} finally {
			try {
				if (rd != null) {
					rd.close();
				}

				if (con != null) {
					con.disconnect();
				}
			} catch (Exception var17) {
				var17.printStackTrace();
				result.addErrorString(var17.getMessage());
			}

		}
	}

}
