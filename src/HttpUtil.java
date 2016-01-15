import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpUtil{

	
	//HTTP POST request
	public static void HttpPost(String json){
		
		String url = "http://128.111.43.52:8080/indexfile";
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()){
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
			

		}
		
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
}