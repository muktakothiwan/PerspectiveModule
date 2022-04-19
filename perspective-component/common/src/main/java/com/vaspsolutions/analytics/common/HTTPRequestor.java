package com.vaspsolutions.analytics.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.inductiveautomation.ignition.common.gateway.HttpURL;
/**
 * A class used to make HTTP requests to a webservices
 * Mainly used ti call a geo-location service.
 * @author YM
 *
 */
public class HTTPRequestor  {

	/**
	 * A method to make an HTTP GET call to the input URL
	 * @param url
	 * @return
	 */
	public String callService(String url)
	{
		String result = null;
		
		
		try {
			URL _urlToRequest = new URL(url);
			HttpURLConnection con = (HttpURLConnection) _urlToRequest.openConnection();
			con.setRequestMethod("GET");
			
			int responseCode = con.getResponseCode();
			//read the response.
			BufferedReader bufferIn = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = bufferIn.readLine()) != null) {
				response.append(inputLine);
			}
			
			//close buffered reader
			bufferIn.close();
			//send the response
			result = response.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return result;
		
	}
}
