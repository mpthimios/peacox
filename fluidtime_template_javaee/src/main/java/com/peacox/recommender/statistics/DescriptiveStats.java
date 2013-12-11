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
import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.library.model.json.request.RequestGetRoute;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.repository.Citytemp;
import com.peacox.recommender.repository.CitytempService;
import com.peacox.recommender.repository.EmissionStatistics;
import com.peacox.recommender.repository.RecommendationDetails;
import com.peacox.recommender.repository.RecommendationDetailsService;
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
	
	@Autowired
	private RecommendationDetailsService recommendationDetailsService;
	
	@Autowired 
	private CitytempService citytempService;
	
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
    
    public void calculateDetailedStats(){
    	List<RecommendationDetails> recommendationDetails = recommendationDetailsService.getAll();
    	log.debug("found: " + recommendationDetails.size());
    	//log.debug("first session id: " + recommendationDetails.iterator().next().getUserRouteRequest().getSessionId());
    	//log.debug("first session id: " + recommendationDetails.iterator().next().getUserRouteRequest().getSessionId());
    	int doesNotContainWalk = 0;
    	int walkAdded = 0;
    	int doesNotContainPT = 0;
    	int ptAdded = 0;
    	int containsCar = 0;
    	int parAdded = 0;
    	int doesNotContainMaxBike = 0;
    	int maxBikeAdded = 0;
    	int maxBikeAddedInTrips = 0;
    	int bikeNotAvailable = 0;
    	int doesNotContainMaxWalk = 0;
    	int maxWalkAdded = 0;
    	int maxWalkAddedInTrips = 0;
    	int walkNotAvailable = 0;
    	int extremeConditions = 0;
    	
    	for (RecommendationDetails recommendationDetail : recommendationDetails){
    		try{
	    		//1. check if walk was included in the request
	    		RequestGetRoute routeRequest = RouteParser	                
						.routeRequestFromJson(recommendationDetail.getUserRouteRequest().getRequest());
	    		String decommpressedRes = null;
	    		decommpressedRes = CompressString.decompress(recommendationDetail.getUserRouteResult().getResult());    		
	    		JsonResponseRoute routeResult = RouteParser
		                .routeFromJson(decommpressedRes);
	    		
	    		
	    		if(!routeRequest.getModality().contains("walk")){
	    			doesNotContainWalk++;
	    			for ( JsonTrip trip : routeResult.getTrips()){
		    			if (trip.getModality().matches("walk")){
		    				walkAdded++;
		    				break;
		    			}
		    		}
	    		}
	    		
	    		if(!routeRequest.getModality().contains("pt")){
	    			doesNotContainPT++;
	    			for ( JsonTrip trip : routeResult.getTrips()){
		    			if (trip.getModality().matches("pt")){
		    				ptAdded++;
		    				break;
		    			}
		    		}
	    		}
	    		
	    		if(routeRequest.getModality().contains("car")){
	    			containsCar++;
	    			for ( JsonTrip trip : routeResult.getTrips()){
		    			if (trip.getModality().matches("par")){
		    				parAdded++;
		    				break;
		    			}
		    		}
	    		}	    			    		
	    		
	    		Date date = recommendationDetail.getTimestamp();
	    		Calendar calendar = Calendar.getInstance();
	    		calendar.setTime(date);
	    		calendar.add(Calendar.HOUR,-3);
	    		date = calendar.getTime();
	    		
    			List<Citytemp> citytemps = citytempService.findCitytempByDate(date);
    			double temp = 20.0;
    			if (citytemps.size() > 0){
    				Citytemp citytemp = citytemps.get(0);
    				temp = citytemp.getTemp();
    				log.debug("temp: " + temp);
    			}
    			
    			if (temp < 5 || temp > 30){
    				extremeConditions++;
    				doesNotContainMaxBike++;
    				doesNotContainMaxWalk++;
    				boolean bikeFound = false;
    				boolean walkFound = false;
    				boolean tripsWalkOk = true;
    				boolean tripsBikeOk = true;
    				for ( JsonTrip trip : routeResult.getTrips()){
		    			if (trip.getModality().matches("walk")){
		    				walkFound = true;
		    				if (trip.getDurationMinutes() <= 15)
		    					maxWalkAdded++;		
		    				else log.debug("walk time: " + trip.getDurationMinutes());
		    			}
		    			else if (trip.getModality().matches("bike")){
		    				bikeFound = true;
		    				if (trip.getDurationMinutes() <= 15)
		    					maxBikeAdded++;
		    				else {		    					
		    					log.debug("bike time: " + trip.getDurationMinutes());		    					
		    					log.debug("from : " + routeResult.getLocationFrom() + " to: " + routeResult.getLocationTo());
		    				}
		    			}
		    			else{
		    				int totalBike = 0;
		    				int totalWalk = 0;
		    				for (JsonSegment segment : trip.getSegments()){
		    					if (segment.getType().matches("bike")){
		    						totalBike += segment.getDurationMinutes();
		    					}
		    					if (segment.getType().matches("walk")){
		    						totalWalk += segment.getDurationMinutes();
		    					}
		    					
		    				}
		    				if (totalBike >15){
		    					tripsBikeOk = false;
		    				}
		    				if (totalWalk >15){
		    					tripsWalkOk = false;
		    				}
		    			}
		    			
		    		}
    				if (!walkFound) bikeNotAvailable++;
    				if (!bikeFound) walkNotAvailable++;
    				if (!tripsBikeOk) maxBikeAddedInTrips++;
    				if (!tripsWalkOk) maxWalkAddedInTrips++;
    			}		    		
	    		
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		
    	}
    	
    	//print stats:
		log.debug("doesNotContainWalk: " + doesNotContainWalk + " walkAdded: " + walkAdded );
		log.debug("doesNotContainPT: " + doesNotContainPT + " ptAdded: " + ptAdded );
		log.debug("containsCar: " + containsCar + " parAdded: " + parAdded );
		log.debug("extremeConditions: " + extremeConditions );
		log.debug("doesNotContainMaxBike: " + doesNotContainMaxBike + " maxBikeAdded: " + maxBikeAdded + " bikeNotAvailable: " + bikeNotAvailable +
				" maxBikeAddedInTrips: " + maxBikeAddedInTrips);
		log.debug("doesNotContainMaxWalk: " + doesNotContainMaxWalk + " maxWalkAdded: " + maxWalkAdded + " walkNotAvailable: " + walkNotAvailable +
				" maxWalkAddedInTrips: " + maxWalkAddedInTrips);
    	
    }

}