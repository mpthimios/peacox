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

import com.fluidtime.brivel.route.json.AttributeListKeys;
import com.fluidtime.brivel.route.json.RouteParser;
import com.fluidtime.library.model.json.request.RequestGetRoute;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.repository.EmissionStatistics;
import com.peacox.recommender.repository.RecommendationDetailsService;
import com.peacox.recommender.repository.Recommendations;
import com.peacox.recommender.repository.RecommendationsService;
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

@Component("FixIDs")
public class FixIDs{

	@Autowired
	private RecommendationsService recommendationsService;
	
	@Autowired
	private UserRouteRequestService userRequestService;
	
	@Autowired
	private UserRouteResultService userResultService;
	
	
	
	protected Logger log = Logger.getLogger(FixIDs.class);
	
    public void fixUserIdInRecommendations(){
    	List<Recommendations> recommendations = recommendationsService.getAll();    	    	
    	
    	for (Recommendations recommendation : recommendations){
    		String decommpressedRecommendation = null;
    		try {
				decommpressedRecommendation = CompressString.decompress(recommendation.getRecommendations());
				JsonResponseRoute routeResponse = RouteParser
		                .routeFromJson(decommpressedRecommendation);						
	    		
	    		String userId = "";
	    		userId = routeResponse.getAttribute(AttributeListKeys.KEY_ROUTE_USERID);
	    		String sessionId = "";
	    		sessionId = routeResponse.getRequest().getSessionId();
	    		
	    		log.debug("changing values for user: " + userId + " and sessionId: " + sessionId);
	    				
	    		recommendation.setUser_id(Long.parseLong(userId));
	    		recommendation.setSessionId(sessionId);
	    		
	    		recommendationsService.update(recommendation);
	    		
	    		log.debug("DONE");
	    		
    		
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    
    public void fixSessionIdInRequests(){
    	List<UserRouteRequest> userRouteRequests = userRequestService.getAll();
    	
    	for (UserRouteRequest userRouteRequest : userRouteRequests){
    		String request = null;
    		try {
    			request = userRouteRequest.getRequest();
				RequestGetRoute routeRequest = RouteParser
		                .routeRequestFromJson(request);						
	    		
	    		String sessionId = "";
	    		sessionId = routeRequest.getSessionId();
	    		
	    		log.debug("changing values for user: " + userRouteRequest.getUser_id() + " and sessionId: " + sessionId);
	    				
	    		userRouteRequest.setSessionId(sessionId);
	    		
	    		userRequestService.update(userRouteRequest);
	    		
	    		log.debug("DONE");
	    		
    		
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    
    public void fixSessionIdInResults(){
    	List<UserRouteResult> results = userResultService.getAll();    	    	
    	
    	for (UserRouteResult result : results){
    		String decommpressedResult = null;
    		try {
    			decommpressedResult = CompressString.decompress(result.getResult());
				JsonResponseRoute routeResponse = RouteParser
		                .routeFromJson(decommpressedResult);						
	    		
	    		String userId = "";
	    		userId = routeResponse.getAttribute(AttributeListKeys.KEY_ROUTE_USERID);
	    		String sessionId = "";
	    		sessionId = routeResponse.getRequest().getSessionId();
	    		
	    		log.debug("changing values for user: " + userId + " and sessionId: " + sessionId);
	    					    		
	    		result.setSessionId(sessionId);
	    		
	    		userResultService.update(result);
	    		
	    		log.debug("DONE");
	    		
    		
    		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    
}