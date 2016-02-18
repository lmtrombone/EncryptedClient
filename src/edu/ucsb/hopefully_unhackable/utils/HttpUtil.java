package edu.ucsb.hopefully_unhackable.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class HttpUtil {
	private static String home = "https://52.34.59.216:8443/";
	
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
	public static boolean HttpPost(String json) {
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

			//System.out.println(result.toString());
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
