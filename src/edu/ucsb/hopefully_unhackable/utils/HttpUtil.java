package edu.ucsb.hopefully_unhackable.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class HttpUtil{
	private static String home = "https://52.34.59.216:8443/";
	//need to test to make sure it works
	//HTTP Get request
	//returns result as HashMap
	/*
	public static HashMap<String, ArrayList<String>> HttpGet(String keyWord){
		
		HashMap<String, ArrayList<String>> encIndex = null;
		String url = "http://52.34.59.216:8080/searchfile?" + keyWord;
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
			HttpGet getRequest = new HttpGet(url);
			
			HttpResponse response = httpClient.execute(getRequest);
			
			if(response.getStatusLine().getStatusCode() != 200){
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			
			String jsonString = EntityUtils.toString(response.getEntity());
			
			JSONObject myjson = new JSONObject(jsonString);

			Iterator<String> keys = myjson.keys();
			while(keys.hasNext()){
				String key = (String) keys.next();
				if(myjson.get(key) instanceof JSONArray){
					System.out.println("word: " + key);
					JSONArray idx = myjson.getJSONArray(key);
					for(int i = 0; i < idx.length(); i++){
						//System.out.println(idx.getString(i));
						if(encIndex.get(key) == null){
							encIndex.put(key, new ArrayList<String>());
						}
						encIndex.get(key).add(idx.getString(i));
					}
				}
				else if(myjson instanceof JSONObject){
					//System.out.println("word: " + key);
					//System.out.println(myjson.getString(key));
					if(encIndex.get(key) == null){
						encIndex.put(key, new ArrayList<String>());
					}
					encIndex.get(key).add(myjson.getString(key));
				}
			}

		}
		
		catch(IOException e){
			e.printStackTrace();
		}
		
		return encIndex;
	}
	*/
	
	//HTTP GET request
	public static Set<StringPair> HttpGet(String keyWord) {	
		Set<StringPair> set = null;
		String url = home + "searchfile?query=" + keyWord;
		//String url = "http://128.111.43.52:8080/searchfile?query=" + keyWord;
		try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpGet getRequest = new HttpGet(url);
		 	HttpResponse response = httpClient.execute(getRequest);
		 	if(response.getStatusLine().getStatusCode() != 200) {
		 		throw new RuntimeException("Failed : HTTP error code : "
		 				+ response.getStatusLine().getStatusCode());
		 	}
		 			
		 	String jsonString = EntityUtils.toString(response.getEntity());
		 	ObjectMapper mapper = new ObjectMapper();
		 	set = mapper.readValue(jsonString, new TypeReference<Set<StringPair>>(){});
		} catch(IOException e) {
			e.printStackTrace();
		}
		 		
		return set;
	}
	
	//HTTP POST request
	public static void HttpPost(String json) {
		String url = home + "indexfile";
		//String url = "http://128.111.43.52:8080/indexfile";
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost(url);
			
			StringEntity input = new StringEntity(json);
			input.setContentType("application/json");
			post.setEntity(input);
			
			HttpResponse response = httpClient.execute(post);
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
			}
			
			BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
			
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			System.out.println(result.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//function for testing json parser
	public static void test() {
		//String jsonString = "{\"example\":\"1\",\"fr\":\"lol\",\"s\":\"up\"}";
		String jsonString = "{\"status\": \"OK\",\"origin_addresses\": [ \"Vancouver\", \"Seattle\" ],\"destination_addresses\": [ \"San Francisco\", \"Victoria\" ]}";
		try {
			JSONObject myjson = new JSONObject(jsonString);
			Iterator<String> keys = myjson.keys();
			while(keys.hasNext()) {
				String key = (String) keys.next();
				if(myjson.get(key) instanceof JSONArray) {
					System.out.println("word: " + key);
					JSONArray idx = myjson.getJSONArray(key);
					for(int i = 0; i < idx.length(); i++) {
						System.out.println(idx.getString(i));
					}
				} else if(myjson instanceof JSONObject) {
					System.out.println("word: " + key);
					System.out.println(myjson.getString(key));
				}
			}
			
			
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

	}
	
	public static void main(String[] args) {
		test();
	}
}