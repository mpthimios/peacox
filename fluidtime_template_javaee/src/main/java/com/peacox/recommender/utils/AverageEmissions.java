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



public class AverageEmissions
{
	public static String[] modes = {
		"ptTrainS",
		"ptTrainR",
		"ptTrainAirport",
		"ptTrainCog",
		"ptCableCar",
		"ptTrain",
		"ptBusNight",
		"ptBusCity",
		"ptMetro",
		"ptTram",
		"walk",
		"bike",
		"ptTaxi",
		"car"
	};
    public static double getAverageEmissions(int mode)
    {        
        switch (mode)
        {
            case 0:                
                return 10.0;                
            case 1:
                return 10.0;
            case 2:
            	return 10.0;
            case 3:
            	return 10.0;
            case 4:
            	return 15.5;
            case 5:
            	return 15.5;
            case 6:
            	return 44.0;
            case 7:
            	return 25.5;
            case 8:
            	return 25.5;
            case 9:
            	return 10.0;
            case 10:
            	return 10.5;
            case 11:
            	return 0;
            case 12:
            	return 0;
            case 13:
            	return 200;
            case 14:
            	return 169;
        }
		return 0;
    }
    
    public static double getLarasAverageEmissions(int mode)
    {        
        switch (mode)
        {   
	        case 0:
	            return 0;
            case 1:
                return 0;
            case 2:
            	return 0;
            case 3:
            	return 169.0;
            case 4:
            	return 169.0;
            case 5:
            	return 25.5;
            case 6:
            	return 10.5;
            case 7:
            	return 10.0;
            case 8:
            	return 0.0;
            case 9:
            	return 0.0;
            case 10:
            	return 0.0;
            case 11:
            	return 10.0;                       
        }
		return 0;
    }
}