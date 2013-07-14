package com.peacox.recommender.statistics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jline.internal.Log;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.util.JRubyFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fluidtime.brivel.route.json.RouteParser;
import com.fluidtime.library.model.json.request.RequestGetRoute;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.repository.EmissionStatistics;
import com.peacox.recommender.repository.Stages;
import com.peacox.recommender.repository.StagesService;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserRouteRequest;
import com.peacox.recommender.repository.UserRouteRequestService;
import com.peacox.recommender.repository.UserRouteResult;
import com.peacox.recommender.repository.UserRouteResultService;
import com.peacox.recommender.repository.UserTreeScores;
import com.peacox.recommender.repository.UserTreeScoresService;
import com.peacox.recommender.utils.CompressString;
import com.peacox.recommender.webservice.Webservice;

@Component("DescriptiveStats")
public class DescriptiveStats{

	@Autowired
	private StagesService stagesService;
	
	@Autowired
	private UserTreeScoresService userTreeScoresService;
	
	@Autowired
	private UserRouteRequestService userRequestService;
	
	@Autowired
	private UserRouteResultService userResultService;
	
	protected Logger log = Logger.getLogger(Webservice.class);
	
    public void calculateRouteRequestStats(){
    	List<UserRouteRequest> routeRequests = userRequestService.getAll();    	    	
    	LinkedHashMap<String, Integer> motStats = new LinkedHashMap<String, Integer>(); 
    	
    	for (UserRouteRequest userRouteRequest : routeRequests){
    		String decommpressedReq = null;
    		try {
				//decommpressedReq = CompressString.decompress(userRouteRequest.getRequest());
				RequestGetRoute routeRequest = RouteParser
		                //.routeRequestFromJson(decommpressedReq);
						.routeRequestFromJson(userRouteRequest.getRequest());
	    		
	    		String modalities = "";
	    		for (String entry : routeRequest.getModality()){
	    			modalities = modalities + "|" + entry;
	    		}
	    		
	    		if (!motStats.containsKey(modalities)){
	    			motStats.put(modalities, 1);
	    		}
	    		else{
	    			int currentCount = motStats.get(modalities);
	    			currentCount++;
	    			motStats.put(modalities, currentCount);
	    		}
    		
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	for (Map.Entry<String, Integer> entry : motStats.entrySet()){
    		log.debug("" + entry.getKey() + " number of times: " + entry.getValue());
    	}
    }
    
    public void calculateRouteResultStats(){
    	List<UserRouteResult> routeResults = userResultService.getAll();    	    	
    	LinkedHashMap<String, Integer> motStats = new LinkedHashMap<String, Integer>(); 
    	
    	for (UserRouteResult userRouteResult : routeResults){
    		String decommpressedRes = null;
    		try {
				decommpressedRes = CompressString.decompress(userRouteResult.getResult());
				RequestGetRoute routeResult = RouteParser
		                .routeRequestFromJson(decommpressedRes);
						
	    		
	    		String modalities = "";
	    		for (String entry : routeResult.getModality()){
	    			modalities = modalities + "|" + entry;
	    		}
	    		
	    		if (!motStats.containsKey(modalities)){
	    			motStats.put(modalities, 1);
	    		}
	    		else{
	    			int currentCount = motStats.get(modalities);
	    			currentCount++;
	    			motStats.put(modalities, currentCount);
	    		}
    		
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	for (Map.Entry<String, Integer> entry : motStats.entrySet()){
    		log.debug("" + entry.getKey() + " number of times: " + entry.getValue());
    	}
    }

}