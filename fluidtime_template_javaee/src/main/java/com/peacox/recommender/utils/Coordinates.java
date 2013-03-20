package com.peacox.recommender.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.httpclient.util.URIUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Coordinates
{
   
    public static String[] GeocodeAddress(String address)
    {
        //List<Coordinates> coordinates = new List<Coordinates>();

        //String response = new WebClient().DownloadString(String.Format("http://maps.googleapis.com/maps/api/geocode/xml?address={0}&sensor=false", HttpUtility.UrlEncode(address)));
        
        URL geocode;
        String lat = "";
        String lon = "";
		try {
			 
			String encodedurl = URIUtil.encodeQuery("https://maps.googleapis.com/maps/api/geocode/json?"+"address="+address+"&sensor=false");
			System.out.println(encodedurl);
			geocode = new URL(encodedurl);
			HttpURLConnection conn = (HttpURLConnection) geocode.openConnection();
	        //conn.setRequestMethod( "POST" );
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                conn.getInputStream()));
	        String inputLine;
	        String response = "";
	        while ((inputLine = in.readLine()) != null) 
	        	response += inputLine;
	        in.close();
	        
	        System.out.println(response);
	        
	        JsonParser parser = new JsonParser();
	        JsonElement jsonElement = parser.parse(response);
	        JsonObject jsonObject = jsonElement.getAsJsonObject();
	        lat = jsonObject.getAsJsonArray("results").get(0)
		        .getAsJsonObject().get("geometry")
		        .getAsJsonObject().get("location")
		        .getAsJsonObject().get("lat").getAsString();
	        lon = jsonObject.getAsJsonArray("results").get(0)
	    	        .getAsJsonObject().get("geometry")
	    	        .getAsJsonObject().get("location")
	    	        .getAsJsonObject().get("lng").getAsString();
	        
	        System.out.println("lat: " + lat + " lon: " + lon);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        //XmlDocument xmlDoc = new XmlDocument();
        //xmlDoc.LoadXml(response);

        //String status = xmlDoc.DocumentElement.SelectSingleNode("status").InnerText;
        String status = "ok";
        switch (status)
        {
            case "ok":
                String[] result = new String[2];
                result[0] = lon;
                result[1] = lat;
                return result;                
            case "zero_results":
                return null;
            case "over_query_limit":
            case "invalid_request":
            case "request_denied":                
        }

//        XmlNodeList nodeCol = xmlDoc.DocumentElement.SelectNodes("result");
//        foreach (XmlNode node in nodeCol)
//        {
//            double lat = Convert.ToDouble(node.SelectSingleNode("geometry/location/lat").InnerText, CultureInfo.InvariantCulture);
//            double lng = Convert.ToDouble(node.SelectSingleNode("geometry/location/lng").InnerText, CultureInfo.InvariantCulture);
//
//            Coordinates wgs84 = new Coordinates() { Latitude = lat, Longitude = lng };
//            coordinates.Add(wgs84);
//        }

        //return coordinates.ToArray();
        return null;
    }
}