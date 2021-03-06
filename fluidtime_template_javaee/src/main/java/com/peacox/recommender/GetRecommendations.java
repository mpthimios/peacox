package com.peacox.recommender;

import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.fluidtime.library.model.json.FeatureTypes.JsonFeature;
import com.fluidtime.library.model.json.request.RequestGetRoute;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.fluidtime.brivel.route.json.AttributeListKeys;
import com.fluidtime.brivel.route.json.RouteParser;
import com.peacox.recommender.messages.Messages;
import com.peacox.recommender.repository.Citytemp;
import com.peacox.recommender.repository.CitytempService;
import com.peacox.recommender.repository.MessagesShown;
import com.peacox.recommender.repository.MessagesShownService;
import com.peacox.recommender.repository.RecommenderMessages;
import com.peacox.recommender.repository.RecommenderMessagesService;
import com.peacox.recommender.repository.Stages;
import com.peacox.recommender.repository.StagesService;
import com.peacox.recommender.repository.UserProfile;
import com.peacox.recommender.repository.UserProfileService;
import com.peacox.recommender.repository.UserRouteRequest;
import com.peacox.recommender.repository.UserRouteRequestService;
import com.peacox.recommender.repository.UserTreeScores;
import com.peacox.recommender.repository.UserTreeScoresService;
import com.peacox.recommender.repository.UserVehicle;
import com.peacox.recommender.repository.UserVehicleService;
import com.peacox.recommender.utils.Reports;
import com.peacox.recommender.utils.TreeScore;
import com.peacox.recommender.webservice.Webservice;
//import com.peacoxrmi.model.User;
import de.bezier.math.combinatorics.Combination;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jline.internal.Log;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

//@Component("GetRecommendations")
public class GetRecommendations{
    
  //Stats
  private LinkedHashMap<String, Double> maxValues = new LinkedHashMap<String, Double>();
  private LinkedHashMap<String, Double> minValues = new LinkedHashMap<String, Double>();
  private LinkedHashMap<String, Double> sumValues = new LinkedHashMap<String, Double>();
  private LinkedHashMap<String, Double> medianValues = new LinkedHashMap<String, Double>();
  private LinkedHashMap<String, Double> meanValues = new LinkedHashMap<String, Double>();
  
  LinkedHashMap<String, Integer> modalitiesInFinalResults = new LinkedHashMap();
  
  private double minWBvalue = 100.0;
  
  private double maxValue = 0;
  private double minValue = 1000000;
  private double sumValue = 0;
  
  private String ownsVehicle = "";
  
  private Integer personalizedWalkingTimeThreshold;  
  private Integer walkingTimeThreshold;  
  private Integer walkingTimeThresholdForCarUsers;
  private Integer walkingTimeThresholdForPTUsers;
  private Integer walkingTimeThresholdForBikeUsers;
  private Integer walkingTimeThresholdForWalkUsers;
  private Integer bikeTimeThresholdForCarUsers;
  private Integer bikeTimeThresholdForPTUsers;
  private Integer bikeTimeThresholdForBikeUsers;
  private Integer bikeTimeThresholdForWalkUsers;
  private int bikeTimeThreshold;  
  private int maxPTroutesToShow;
  private double timeThresholdCutoff;
  private String messageForPT = "";
  private String messageForWalk = "";
  private String messageForPT_De = "";
  private String messageForWalk_De = "";
  private double thresholdForParkAndRide;
  private double walkBikeThresholdForOmmitingCarParRoutes;
  private double ptChangesFactorThreshold;
  private int maxBikeTimeInExtremeConditions = 15;
  private int maxWalkTimeInExtremeConditions = 15;
  
  private int maxPtChangesThreshold = 4;
  
  protected Logger log = Logger.getLogger(GetRecommendations.class);
  
  protected boolean SHOWALL = false;
  protected boolean showMessageForPT = true;
  protected boolean showMessageForWalk = true;
  
  //context variables
  boolean RouteInWalkingDistance; //OK
  boolean ComparableCarAndPTRouteDuration; //OK  
  boolean HighEmmissionsDifferenceBetweenCarAndPT; //OK
  boolean TooManyCarRoutes; //OK
  boolean TooManyPTRoutes; //OK
  boolean EmmissionsIncreasing; //OK 
  boolean EmmissionsHighComparedToOtherUsers; //OK 
  boolean NiceWeather; //OK
  //somthing else about the score?
  
  boolean askedForCar = false;
  boolean askedForPT = false;
  boolean askedForWalk = false;
  boolean askedForBike = false;
  
  UserProfile userProfile = null;
  
  @Autowired protected UserRouteRequestService routeRequestService;
  
  @Autowired protected StagesService stagesService;
  
  @Autowired protected CitytempService citytempService;
  
  @Autowired private ApplicationContext appContext;
  
  @Autowired private UserTreeScoresService userTreeScoresService;
  
  @Autowired protected MessagesShownService messagesShownService;
  
  @Autowired protected RecommenderMessagesService recommenderMessagesService;
  
  @Autowired protected UserVehicleService userVehicleService;
  
  @Autowired protected UserProfileService userProfileService;
  
  public LinkedHashMap getRecommendations(UserPreferences userPreferences, ArrayList<JsonResponseRoute> routeResults,
		  String city){
        
	  log.warn("Start processing RouteRecommendations");
	  
	  minValues = new LinkedHashMap<String, Double>();
	  sumValues = new LinkedHashMap<String, Double>();
	  medianValues = new LinkedHashMap<String, Double>();
	  meanValues = new LinkedHashMap<String, Double>();
	  minWBvalue = 100.0;
	  
	  LinkedHashMap finalRouteResults;
        updateTotalDurationStats(routeResults);
        updateTotalWBDurationStats(routeResults);
        updateTotalEmissionStats(routeResults, city, null);
        
        //HashMap<String, Double> statistics = updateStatistics(routeResults);
        
        switch(((Double)userPreferences.getOrderAlgorithm()).intValue()){
            case 1: finalRouteResults = methodForRecommendations1(userPreferences, 
            		routeResults, 1, null, city, null);
                break;
            case 2: finalRouteResults = methodForRecommendations2(userPreferences, routeResults, city, null);
                break;                                                                          
            default: finalRouteResults = methodForRecommendations1(userPreferences, 
            		routeResults, 1, null, city, null);
                break;                                        
        }
        return finalRouteResults;
    }
  
  public LinkedHashMap getRecommendations(UserPreferences userPreferences, 
		  ArrayList<JsonResponseRoute> routeResults, long user_id, String city){
	  
	  log.warn("Start processing RouteRecommendations. user_id: " + user_id);
	  log.warn("loaded property getWalkingTimeThreshold: " + this.getWalkingTimeThreshold());
	  log.warn("loaded property walkingTimeThresholdForCarUsers: " + this.getWalkingTimeThresholdForCarUsers());
	  log.warn("loaded property walkingTimeThresholdForPTUsers: " + this.getWalkingTimeThresholdForPTUsers());
	  log.warn("loaded property walkingTimeThresholdForBikeUsers: " + this.getWalkingTimeThresholdForBikeUsers());
	  log.warn("loaded property walkingTimeThresholdForWalkUsers: " + this.getWalkingTimeThresholdForWalkUsers());
	  log.warn("loaded property bikeTimeThresholdForCarUsers: " + this.getBikeTimeThresholdForCarUsers());
	  log.warn("loaded property bikeTimeThresholdForPTUsers: " + this.getBikeTimeThresholdForPTUsers());
	  log.warn("loaded property bikeTimeThresholdForBikeUsers: " + this.getBikeTimeThresholdForBikeUsers());
	  log.warn("loaded property bikeTimeThresholdForWalkUsers: " + this.getBikeTimeThresholdForWalkUsers());
	  log.warn("loaded property bikeTimeThreshold: " + this.getBikeTimeThreshold());
	  log.warn("loaded property maxPTroutesToShow: " + this.getMaxPTroutesToShow());
	  log.warn("loaded property timeThresholdCutoff: " + this.getTimeThresholdCutoff());
	  log.warn("loaded property messageForPT: " + this.getMessageForPT());
	  log.warn("loaded property messageForWalk: " + this.getMessageForWalk());
	  log.warn("loaded property messageForPT: " + this.getMessageForPT_De());
	  log.warn("loaded property messageForWalk: " + this.getMessageForWalk_De());
	  
	  LinkedHashMap<Integer, HashMap<JsonTrip, Double>> finalRouteResults;
	  
	  UserVehicle userVehicle = userVehicleService.findUserVehicleByUserId(user_id);
	  
	  
      minValues = new LinkedHashMap<String, Double>();
	  sumValues = new LinkedHashMap<String, Double>();
	  medianValues = new LinkedHashMap<String, Double>();
	  meanValues = new LinkedHashMap<String, Double>();
	  minWBvalue = 100.0;
      updateTotalDurationStats(routeResults);
      updateTotalWBDurationStats(routeResults);
      updateTotalEmissionStats(routeResults, city, userVehicle);
      showMessageForPT = true;
      showMessageForWalk = true;
      RequestGetRoute routeRequest = null;
      //initialize context variables
      RouteInWalkingDistance = false;
      ComparableCarAndPTRouteDuration = false;  
      HighEmmissionsDifferenceBetweenCarAndPT = false;
      TooManyCarRoutes = false;
      TooManyPTRoutes = false;
      EmmissionsIncreasing = false;
      EmmissionsHighComparedToOtherUsers = false;
      NiceWeather = false;
      
      askedForCar = false;
      askedForPT = false;
      askedForWalk = false;
      askedForBike = false;
             
      
      String sessionID="";
      
	      //get actual preferences the user has already set
	      //UserRouteRequest userRouteRequest = routeRequestService.findRouteRequestByUserIdTimestamp(user_id);
      log.warn("Finding Original Route Request for SessionId: " + routeResults.get(0).getRequest().getSessionId());
      UserRouteRequest userRouteRequest = routeRequestService.findRouteRequestByUserIdAndSessionId(user_id, 
    		  routeResults.get(0).getRequest().getSessionId());
      sessionID = routeResults.get(0).getRequest().getSessionId();
	     
	  if (userRouteRequest != null){
	      routeRequest = RouteParser
	              .routeRequestFromJson(userRouteRequest.getRequest());
	      
	      //check if we have to show messages for PT and Walk
	      if (!routeRequest.getModality().contains("pt")){
	    	  this.showMessageForPT = true;	    	  
	      }
	      if (!routeRequest.getModality().contains("walk")){
	    	  this.showMessageForWalk = true;
	      }
	      
	    //check if we have to show messages for PT and Walk
	      if (routeRequest.getModality().contains("pt")){
	    	  askedForPT = true;	    	  
	      }
	      if (routeRequest.getModality().contains("walk")){
	    	  askedForWalk = true;
	      }
	      if (routeRequest.getModality().contains("car")){
	    	  askedForCar = true;
	      }
	      if (routeRequest.getModality().contains("bike")){
	    	  askedForBike = true;
	      }
	      
	      log.warn("route request found - askedForPT: " + askedForPT + " askedForWalk: " + askedForWalk +
	    		  " askedForCar: " + askedForCar + " askedForBike: " + askedForBike);
	      
	      HashSet<String> requestedModalities = new HashSet<String>();
	      try{
	    	  requestedModalities = routeRequest.getModality();
	      }
	      catch (Exception e){
	    	  log.error("Could not load user requested modalities");
	      }
	      
	      boolean comfortableFound = false;
	      boolean barrierFreeFound = false;
	      boolean fastFound = false;
	      
	      //Checking for comfortable:
	      log.warn("Going to check which options to set");
	      
	      try{
	    	  log.warn("Checking for Comfortable heuristics: ");
		      log.warn("PtMaxChanges: " + routeRequest.getOptionsRoute().getPtMaxChanges() +
		    		  " PtMaxWalkingTime: " + routeRequest.getOptionsRoute().getPtMaxWalkingTime());
	    	  if ((routeRequest.getOptionsRoute().getPtMaxChanges() <= 2 
		    		   &&
		    		  routeRequest.getOptionsRoute().getPtMaxWalkingTime() <=10)){
	    		  log.warn("Found heuristic comfortable");
	    		  comfortableFound = true;
	    	  }
	      }catch (Exception e){
	    	  log.warn("Could not check for comfortable heuristics");
	      }
	      
	      log.warn("Checking for Comfortable option: ");
	      
	      try{
	    	  log.warn("appName: " + routeRequest.getDetails().getAppName());
	    	  if (routeRequest.getDetails().getAppName().matches("comfortable")){
	    		  log.warn("Found appName comfortable");
	    		  comfortableFound = true;
	    	  }
	      }catch (Exception e){
	    	  log.warn("Could not check for comfortable option");
	      }
	      
	      //Checking for barrier-free:	      
	      
	      
	      try{
	    	  log.warn("Checking for Barrier-free heuristics: ");
	    	  log.warn("ptNoStairs: " + routeRequest.getOptionsRoute().getPtMobilityConstraints().contains("ptNoStairs") +
		    		  " ptNoEscalators: " + routeRequest.getOptionsRoute().getPtMobilityConstraints().contains("ptNoEscalators") +
		    		  " ptUseWheelchair " + routeRequest.getOptionsRoute().getPtMobilityConstraints().contains("ptUseWheelchair"));
	    	  if (routeRequest.getOptionsRoute().getPtMobilityConstraints().contains("ptNoStairs") &&
		    		  routeRequest.getOptionsRoute().getPtMobilityConstraints().contains("ptNoEscalators") &&
		    		  routeRequest.getOptionsRoute().getPtMobilityConstraints().contains("ptUseWheelchair")){
		    	  log.warn("Found heuristic barrier-free");
		    	  barrierFreeFound = true;
	    	  }
	      }catch (Exception e){
	    	  log.warn("Could not check for barrier-free heuristics");
	      }
	      
	      log.warn("Checking for Barrier-free option: ");
	      log.warn("appName: " + routeRequest.getDetails().getAppName());
	      try{
	    	  if (routeRequest.getDetails().getAppName().matches("barrier-free")){
	    		  log.warn("Found appName barrier-free");
	    		  barrierFreeFound = true;
	    	  }
	      }catch (Exception e){
	    	  log.warn("Could not check for barrier-free option");
	      }
	      
	      //Checking for fast:	      	      
	      try{
	    	  log.warn("Checking for Fast heuristics: ");
		      log.warn("ptMinTime: " + routeRequest.getOptionsRoute().getPtRouteOptimisation().matches("ptMinTime") +
		    		  " carMinTime: " + routeRequest.getOptionsRoute().getCarRouteOptimisation().matches("carMinTime"));
	    	  if (routeRequest.getOptionsRoute().getPtRouteOptimisation().matches("ptMinTime")&&
		    		  routeRequest.getOptionsRoute().getCarRouteOptimisation().matches("carMinTime")){
		    	  log.warn("Found heuristic fast");
		    	  fastFound = true;
	    	  }
	      }catch (Exception e){
	    	  log.warn("Could not check for fast heuristics");
	      }
	      
	      log.warn("Checking for Fast option: ");
	      log.warn("appName: " + routeRequest.getDetails().getAppName());
	      try{
	    	  if (routeRequest.getDetails().getAppName().matches("fast")){
	    		  log.warn("Found appName fast");
	    		  fastFound = true;
	    	  }
	      }catch (Exception e){
	    	  log.warn("Could not check for fast option");
	      }
	      
	      log.warn("Options set: comfortableFound - " + comfortableFound +
	    		  ", barrierFreeFound - " + barrierFreeFound + ", " +
	    		  "fastFound - " + fastFound);
	      
	      //set user preferences according to Comfortable if applicable:
	      if (comfortableFound){
	
	    	  // this is a default option for comfortable?
	    	  log.warn("Setting options for comfortable profile");
	    	  //comfortable?
	    	  userPreferences.setComfortHigh(10.0);
	    	  userPreferences.setComfortMedium(4.0);
	    	  userPreferences.setComfortLow(1.0);
	    	  userPreferences.setComfortImportance(0.4);
	    	  
	    	  //it means that I don't care much about the time
	    	  userPreferences.setDuration10min(4.0);
	    	  userPreferences.setDuration30min(10.0);
	    	  userPreferences.setDuration30plus(1.0);
	    	  userPreferences.setDurationImportance(0.2);
	    	  
	    	  //it means that I wouldn't mind walking a bit
	    	  userPreferences.setWB10min(4.0);
	    	  userPreferences.setWB30min(10.0);
	    	  userPreferences.setWB30plus(1.0);
	    	  userPreferences.setWbtimeImportance(0.2);
	    	  
	    	  userPreferences.setEcoAttitudeImportance(0.2);
	      }
	      
	     //set user preferences according to barrierFree if applicable:
          if (barrierFreeFound){
	    	  
	    	  // this is a default option for barrier-free?
	    	  log.warn("Setting options for barrier-free profile");
	    	  
	    	  //very comfortable?
	    	  userPreferences.setComfortHigh(10.0);
	    	  userPreferences.setComfortMedium(4.0);
	    	  userPreferences.setComfortLow(1.0);
	    	  userPreferences.setComfortImportance(0.5);
	    	  
	    	  //it means that I don't care much about the time
	    	  userPreferences.setDuration10min(1.0);
	    	  userPreferences.setDuration30min(4.0);
	    	  userPreferences.setDuration30plus(10.0);
	    	  userPreferences.setDurationImportance(0.1);
	    	  
	    	  //it means that I don't want to be walking or taking a bike
	    	  userPreferences.setWB10min(10.0);
	    	  userPreferences.setWB30min(4.0);
	    	  userPreferences.setWB30plus(1.0);
	    	  userPreferences.setWbtimeImportance(0.2);
	    	  
	    	  userPreferences.setEcoAttitudeImportance(0.2);
	      }
	     
	      
          if (fastFound || (!fastFound && !barrierFreeFound && !comfortableFound)){
	    	  
	    	  // this is a default option for fast?
	    	  
	    	  log.warn("Setting options for fast profile");
	    	  
	    	  //I don't care about comfort?
	    	  userPreferences.setComfortHigh(3.0);
	    	  userPreferences.setComfortMedium(4.0);
	    	  userPreferences.setComfortLow(1.0);
	    	  userPreferences.setComfortImportance(0.2);
	    	  
	    	  //it means that I care much about the time, I want to to be the minimum possible
	    	  userPreferences.setDuration10min(10.0);
	    	  userPreferences.setDuration30min(4.0);
	    	  userPreferences.setDuration30plus(1.0);
	    	  userPreferences.setDurationImportance(0.5);
	    	  
	    	  //it means that I care about walking or taking a bicycle
	    	  userPreferences.setWB10min(10.0);
	    	  userPreferences.setWB30min(4.0);
	    	  userPreferences.setWB30plus(1.0);
	    	  userPreferences.setWbtimeImportance(0.1);
	    	  
	    	  userPreferences.setEcoAttitudeImportance(0.2);	    	  
	      }
	      
	  }
	  else{
		  askedForBike = true;
		  askedForCar = true;
		  askedForPT = true;
		  askedForWalk = true;
		  log.warn("route request NOT found - askedForPT: " + askedForPT + " askedForWalk: " + askedForWalk +
	    		  " askedForCar: " + askedForCar + " askedForBike: " + askedForBike);
	  }
	  
	  userProfile = userProfileService.findUserProfileByUserId(user_id);
	  
	  if (userProfile != null){
		  int mobilityBehaviour = userProfile.getMobility_behaviour();
		  
		  switch (mobilityBehaviour){
		  	case 1:
		  		personalizedWalkingTimeThreshold = getWalkingTimeThresholdForCarUsers();		  		
		  		log.warn("user likes taking the car");
		  		break;
		  	case 2:
		  		personalizedWalkingTimeThreshold = getWalkingTimeThresholdForPTUsers();
		  		log.warn("user likes taking public transport");
		  		break;
		  	case 3:
		  		personalizedWalkingTimeThreshold = getWalkingTimeThresholdForBikeUsers();
		  		log.warn("user likes taking the bike");
		  		break;
		  	case 4:
		  		personalizedWalkingTimeThreshold = getWalkingTimeThresholdForWalkUsers();
		  		log.warn("user likes to walk");
		  		break;
		  	default:
		  		personalizedWalkingTimeThreshold = getWalkingTimeThreshold();	  			
		  }
	  }
	  else{
		  log.warn("user profile: " + userProfile + " " + askedForWalk);
		  personalizedWalkingTimeThreshold = getWalkingTimeThreshold();
	  }
	  
	  log.warn("personalizedWalkingTimeThreshold: " + personalizedWalkingTimeThreshold);
	  
      switch(((Double)userPreferences.getOrderAlgorithm()).intValue()){
          case 1: finalRouteResults = methodForRecommendations1(userPreferences, routeResults, 
        		  user_id, routeRequest, city, userVehicle);
              break;
          case 2: finalRouteResults = methodForRecommendations2(userPreferences, routeResults, city, userVehicle);
              break;                                                                          
          default: finalRouteResults = methodForRecommendations1(userPreferences, routeResults, 
        		  user_id, routeRequest, city, userVehicle);
              break;                                        
      }
      try{
    	  Messages messages = (Messages) appContext.getBean("Messages");
	      //log context variables:
	      log.warn("---- Context Variables ----");
	      log.warn("RouteInWalkingDistance: " +  RouteInWalkingDistance);
	      log.warn("ComparableCarAndPTRouteDuration: " +  ComparableCarAndPTRouteDuration);
	      log.warn("HighEmmissionsDifferenceBetweenCarAndPT: " +  HighEmmissionsDifferenceBetweenCarAndPT);
	      log.warn("TooManyCarRoutes: " +  TooManyCarRoutes);
	      log.warn("TooManyPTRoutes: " +  TooManyPTRoutes);       
	      log.warn("EmmissionsIncreasing: " +  EmmissionsIncreasing);
	      log.warn("EmmissionsHighComparedToOtherUsers: " +  EmmissionsHighComparedToOtherUsers);
	      log.warn("NiceWeather: " +  NiceWeather);      
	      
	      //first get all the messages from the database
	      List<RecommenderMessages> listOfRecommenderMessages = recommenderMessagesService.getRecommenderMessages();
	      
	      LinkedHashMap<RecommenderMessages, Double> messagesAndUtilities = new LinkedHashMap();
	      //init
	      //for (RecommenderMessages message : listOfRecommenderMessages){
	     //  messagesAndUtilities.put(message, -1000.0);
	      //}
	      
	      log.warn("some stats on the presented modalities: ");
	      for (Map.Entry<String, Integer> entry : modalitiesInFinalResults.entrySet()){
	    	  log.warn(" for modality: " + entry.getKey() + " we have: " + entry.getValue() + " results");
	      }
	      
	      //if we are going to show a route that is not requested then we should information the user here
	      if (modalitiesInFinalResults.containsKey("walk") && askedForWalk == false){
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){		    	  
				  if (((JsonTrip) entry.getValue().entrySet().iterator().next()
						  .getKey()).getModality().matches("walk")){
					  if (city.matches("vienna")){
						  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
						  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,getMessageForWalk());
					  }
					  else if (city.matches("dublin")){
						  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
						  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,getMessageForWalk());
					  }
					  log.warn("message added: " + getMessageForWalk() + " city: " + city);
					  break;
				  }
			  }
	    	  return finalRouteResults;
	      }
	      
	      if (modalitiesInFinalResults.containsKey("bike") && askedForBike == false){
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){		    	  
				  if (((JsonTrip) entry.getValue().entrySet().iterator().next()
						  .getKey()).getModality().matches("bike")){
					  if (city.matches("vienna")){
						  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
						  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,getMessageForWalk_De());
					  }
					  else if (city.matches("dublin")){
						  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
						  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,getMessageForWalk());
					  }
					  log.warn("message added: " + getMessageForWalk() + " city: " + city);
					  break;
				  }
			  }
	    	  return finalRouteResults;
	      }
	      
	      if (modalitiesInFinalResults.containsKey("pt") && askedForPT == false){
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){		    	  
				  if (((JsonTrip) entry.getValue().entrySet().iterator().next()
						  .getKey()).getModality().matches("pt")){
					  if (city.matches("vienna")){
						  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
						  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,getMessageForPT_De());
					  }
					  else if (city.matches("dublin")){
						  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
						  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,getMessageForPT());
					  }
					  log.warn("message added: " + getMessageForPT() + " city: " + city);
					  break;
				  }
			  }
	    	  return finalRouteResults;
	      }
	      
	      double[] contextUtilities = new double[9];
	      
	      for (int i = 0; i < 9; i++){
	    	  contextUtilities[i] = 0.0;
	      }
	      
	      if (RouteInWalkingDistance){
	    	  contextUtilities[0] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("walk")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"It's a short distance! You might as well walk!");
	    			  break;
	    		  }
	    	  }
	    	  */
	      }
	      if (ComparableCarAndPTRouteDuration){
	    	  contextUtilities[1] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("pt")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"Consider taking public transportation! It takes about the same time.");
	    			  break;
	    		  }
	    	  }
	    	  */
	      }
	      if (HighEmmissionsDifferenceBetweenCarAndPT){
	    	  contextUtilities[2] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("car")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"The emmissions of taking a car are too high.");
	    			  break;
	    		  }
	    	  }
	    	  */
	      }
	      if (TooManyCarRoutes){
	    	  contextUtilities[3] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("pt")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"Why don't you use public transportation for a change?");
	    			  break;
	    		  }
	    	  }
	    	  */
	      }
	      if (TooManyPTRoutes){
	    	  contextUtilities[4] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("walk")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"Why don't you walk for a change?");
	    			  break;
	    		  }
	    		  else if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("bike")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	  			  		addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"Why don't you take a bike for a change?");
	    			  break;
	    		  }
	    	  }
	    	  */
	      }
	      if (EmmissionsIncreasing){
	    	  contextUtilities[5] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("pt")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"Your emmissions are increasing, consider changing to greener means!");
	    			  break;
	    		  }
	    	  }*/
	      }
	      if (EmmissionsHighComparedToOtherUsers){
	    	  contextUtilities[6] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    		  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("pt")){
	    			  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
	    			  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"Other peacox users have lower emmissions! Consider this route.");
	    			  break;
	    		  }
	    	  }
	    	  */
	      }
	      if (NiceWeather){
	    	  contextUtilities[7] +=1;
	    	  /*
	    	  for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
		    	  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("walk")){
					  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
					  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"The weather looks fine, why don't you walk?");
					  break;
				  }
				  else if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches("bike")){
					  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
					  		addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,"The weather looks fine, why don't you take a bike?");
					  break;
				  }
	    	  }*/
	      }
	      
	      double[] messagesShownStrategies = new double[8];
	      
	      for (int i = 0; i < 8; i++){
	    	  messagesShownStrategies[i] = 0.0;
	      }
	      
	      double[] messagesShownTransportationMeans = new double[4];
	      
	      for (int i = 0; i < 4; i++){
	    	  messagesShownTransportationMeans[i] = 0.0;
	      }
	      
	      List<MessagesShown> messagesShownList =  messagesShownService.findMessagesShownByUserId(user_id);
	      HashMap<Integer, Double> messagesShownStats = new HashMap<Integer, Double>(); 
	      for (MessagesShown message : messagesShownList){
	    	  int strategy = message.getStrategy();
	    	  if (strategy > 0){
	    		  messagesShownStrategies[strategy-1] += 1;
	    	  }
	    	  int transportation_means = message.getTransportation_means();
	    	  if (transportation_means > 0){
	    		  messagesShownTransportationMeans[transportation_means-1] += 1;
	    	  }
	    	  
	    	  if (messagesShownStats.containsKey(message.getMessage_id())){
	    		  double temp = messagesShownStats.get(message.getMessage_id());
	    		  messagesShownStats.put(message.getMessage_id(), temp+1);
	    	  }
	    	  else{
	    		  messagesShownStats.put(message.getMessage_id(), 1.0);
	    	  }
	      }
	      
	      
	      for (RecommenderMessages message : listOfRecommenderMessages){
	    	  double totalMessageUtility = 0.0;
	    	  if (message.getStrategy() > 0){
	    		  totalMessageUtility-=messagesShownStrategies[message.getStrategy()-1];
	    	  }
	    	  if (message.getTransportation_means()>0){
	    		  totalMessageUtility-=messagesShownTransportationMeans[message.getTransportation_means()-1];
	    	  } 
	    	  if (messagesShownStats.containsKey(message.getId())){
	    		  log.warn("reducing utility for messageid: " + message.getId()  + " " + messagesShownStats.get(message.getId()));
	    		  totalMessageUtility-=messagesShownStats.get(message.getId());
	    	  } 
	    	  if (message.getContext() > 0 && contextUtilities[message.getContext()-1] > 0){
	    		  totalMessageUtility+=contextUtilities[message.getContext()-1];
	    		  messagesAndUtilities.put(message, totalMessageUtility);
	    	  }
	      }
	      
	      
	      
	      LinkedHashMap<RecommenderMessages, Double> sortedMessage = this.sortByValue(messagesAndUtilities);
	      
	      for (Map.Entry<RecommenderMessages, Double> entry : sortedMessage.entrySet()){
	    	  log.warn("id: " + entry.getKey().getId() + " message: " + entry.getKey().getText() + " context: " + entry.getKey().getContext() + " utility: " + entry.getValue());
	      }
	      
	      RecommenderMessages messageToShow = sortedMessage.entrySet().iterator().next().getKey();
	      int modalityCode = messageToShow.getTransportation_means();
	      String[] modalities = new String[4];
	      modalities[0] = "car";
	      modalities[1] = "pt";
	      modalities[2] = "bike";
	      modalities[3] = "walk";
	      String selectedModality = "";
	      if (modalityCode > 0){
	    	  selectedModality = modalities[modalityCode-1];
	    	  log.warn("selected modality found");
	      }else{
	    	  selectedModality = "pt";
	    	  log.warn("selected modality not found - choosing pt");
	      }
	      int message_id = 0;
	      for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalRouteResults.entrySet()){
	    	  log.warn("checking to add message: " + ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality());
			  if (((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).getModality().matches(selectedModality)){
				  if (city.matches("vienna")){
					  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
					  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,messageToShow.getText_de());
				  }
				  else if (city.matches("dublin")){
					  ((JsonTrip) entry.getValue().entrySet().iterator().next().getKey()).
					  	addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC,messageToShow.getText());
				  }
				  log.warn("message added: " + messageToShow.getText() + messageToShow.getText_de() + " city: " + city);
				  message_id = messageToShow.getId();
				  break;
			  }
		  }
	      
	    //save
	  	MessagesShown newMessagesShown = new MessagesShown();
	  	newMessagesShown.setOid("Vienna");
	  	newMessagesShown.setUser_id(user_id);
	  	newMessagesShown.setStrategy(messageToShow.getStrategy());
	  	newMessagesShown.setSession_id(sessionID);
	  	newMessagesShown.setContext(messageToShow.getContext());
	  	newMessagesShown.setTransportation_means(messageToShow.getTransportation_means());
	  	newMessagesShown.setTimestamp(new Date());
	  	newMessagesShown.setMessage_id(message_id);
	  	messagesShownService.create(newMessagesShown);
      }catch (Exception e){
    	  e.printStackTrace();
    	  log.warn("cannot add message");
      }
  	
      return finalRouteResults;
  }
    
    private LinkedHashMap methodForRecommendations1(UserPreferences userPreferences, 
        ArrayList<JsonResponseRoute> routeResults, long userId, 
        RequestGetRoute routeRequest, String city, UserVehicle userVehicle){
    	
    	
		log.warn("methodForRecommendations1");
        
    	ArrayList tripsList = new ArrayList<JsonTrip>();
    	
    	boolean extremeConditions = checkForExtremeWeather(city);
    	
    	if (!extremeConditions) {
    		NiceWeather = true; 
    	}
    	
    	for(JsonResponseRoute route : routeResults){
      	  for(JsonTrip trip : route.getTrips()){
      		  
      		  log.warn("emissions: " + trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
      		  if (trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2) == null){
      			trip.addAttribute(AttributeListKeys.KEY_SEGMENT_CO2, Double.toString(this.getTripTotalEmissions(trip, city, userVehicle)));
      		  }
      		  tripsList.add(trip);
      	  }
      	}
    	
        Combination combination = new Combination(tripsList.size(), tripsList.size());//3);
        //ArrayList<String> myArr = new ArrayList<String>();
        LinkedHashMap combinationsAndUtilities = new LinkedHashMap<ArrayList<JsonTrip>, Double>();
        HashMap tripsAndUtilities = new HashMap<JsonTrip, Double>();
        double howMany = 0.0;
        try{ 
            while (combination.hasMore()){
                    int[] combi = combination.next();
                    log.warn("new combination found");
                    double utility = 0.0;
                    ArrayList<Integer> myArr = new ArrayList<Integer>();

                    double totalDuration = 0.0;
                    for (int temp = 0; temp < combi.length; temp++){
                    	log.warn(" combi: " + combi[temp]);
                        int pos = combi[temp];
                        myArr.add(pos);
                        JsonTrip tripResult = (JsonTrip) tripsList.get(pos);
                        double tripUtility = 0.0;
                        switch(((Double)userPreferences.getUtilityAlgorithm()).intValue()){
                            case 0: tripUtility += routeUtilityCalulation(tripResult, userPreferences);                                        
                                break;
                            case 1: tripUtility += routeUtilityCalulation(tripResult, userPreferences);                                        
                                break;
                            case 2: tripUtility += routeUtilityCalulationP2(tripResult, userPreferences);
                                break;
                            case 3: tripUtility += routeUtilityCalulationP3(tripResult, userPreferences);
                                break;
                            case 4: tripUtility += routeUtilityCalulationP4(tripResult, userPreferences, city, userVehicle);
                                break;
                            case 5: tripUtility += routeUtilityCalulationP5(tripResult, userPreferences, city, userVehicle);
                            break;
                            case 6: tripUtility += routeUtilityCalulationP6(tripResult, userPreferences, city, userVehicle);
                            break;
                            default: tripUtility += routeUtilityCalulation(tripResult, userPreferences);
                                break;                                        
                        }
                        utility += tripUtility;
                        if (!tripsAndUtilities.containsKey(tripResult)){
                        	tripsAndUtilities.put(tripResult, tripUtility);
                        }
                        //utility += routeUtilityCalulation(routeResult, userPreferences);
                        totalDuration += (double)getTripTotalDuration(tripResult);
                    }                    
                    combinationsAndUtilities.put(myArr, utility); //howMany should be replaced by utility
                    log.warn("Route List no: " + howMany + " - utility: " + utility);
                    combinationsAndUtilities.put(myArr, utility);
                    howMany++;
            }
        }
        catch(Exception e){
                e.printStackTrace();	
        }
        
        log.warn("combinationsAndUtilities size: " + combinationsAndUtilities.size());
        log.warn("number of routes fetched: " + routeResults.size());

        ArrayList<Double> combinationsAndUtilitiesArray = new ArrayList<Double>(combinationsAndUtilities.values());
        Double maxValue = Collections.max(combinationsAndUtilitiesArray);
        log.warn("max value utility: " + maxValue);	
        int  index = combinationsAndUtilitiesArray.indexOf(maxValue);
        log.warn("index of list with max value: " + index);
        
        LinkedHashMap<Integer, HashMap<JsonTrip, Double>> finalTripResults = new LinkedHashMap<Integer, HashMap<JsonTrip, Double>>();
        LinkedHashMap<Integer, HashMap<JsonTrip, Double>> omittedTripResults = new LinkedHashMap<Integer, HashMap<JsonTrip, Double>>();

        ArrayList finalTrips = null;
        log.warn("before iterating routes");
        int itCounter = 0;
        Iterator it=combinationsAndUtilities.keySet().iterator();
        while ( it.hasNext()) {
                if (itCounter == index){
                        finalTrips = (ArrayList)it.next();
                }
                else{
                        it.next();
                }
                itCounter++;
        }
        
        log.warn("after iterating routes");
        //(Integer[])(new ArrayList<Integer[]>(combinationsAndUtilities.keySet())).get(index);

        log.warn("before finding the route");
        //a variable to hold the routes already in the list
        ArrayList<Integer> topListValues = new ArrayList<Integer>();
        int tripCounter = 0;
        for (int temp = 0; temp < finalTrips.size(); temp++){
                JsonTrip trip = (JsonTrip) (tripsList.get((Integer)finalTrips.get(temp)));
                HashMap tripWithUtility = new HashMap<JsonTrip, Double>();
                tripWithUtility.put(trip, tripsAndUtilities.get(trip));
                finalTripResults.put(tripCounter,tripWithUtility);
                topListValues.add((Integer)finalTrips.get(temp));
                tripCounter++;
        }
        
        //find user preferences per group
        LinkedHashMap<Integer, Double> preferencesPerGroup = this.calculateUserPreferences(userId);
        
        //find user habits per group
        LinkedHashMap<Integer, Double> habitsPerGroup = this.calculateUserHabits(userId);
        
        //check if the user has been actively taking her car
        if (habitsPerGroup.entrySet().iterator().next().getKey() == 1){
        	TooManyCarRoutes = true;
        }
        
        //check if the user has been actively using public transport
        if (habitsPerGroup.entrySet().iterator().next().getKey() == 2){
        	TooManyPTRoutes = true;
        }
        
        LinkedHashMap<Integer, Double> tmpPreferences = new LinkedHashMap<Integer, Double>();
        //check if the user likes taking the car
        if (preferencesPerGroup.entrySet().iterator().next().getKey() == 1){
        	//if yes then re-arrange like this: 2,1,4,3
        	tmpPreferences.put(2, preferencesPerGroup.get(2));
        	tmpPreferences.put(1, preferencesPerGroup.get(1));
        	tmpPreferences.put(4, preferencesPerGroup.get(4));
        	tmpPreferences.put(3, preferencesPerGroup.get(3));        
        }
        //check if the user likes taking public transportation
        else if (preferencesPerGroup.entrySet().iterator().next().getKey() == 2){
        	//if yes then re-arrange like this: 2,4,3,1
        	tmpPreferences.put(2, preferencesPerGroup.get(2));
        	tmpPreferences.put(4, preferencesPerGroup.get(4));
        	tmpPreferences.put(3, preferencesPerGroup.get(3));
        	tmpPreferences.put(1, preferencesPerGroup.get(1));        	        
        }
        else {
        	tmpPreferences.put(4, preferencesPerGroup.get(4));
        	tmpPreferences.put(3, preferencesPerGroup.get(3));
        	tmpPreferences.put(2, preferencesPerGroup.get(2));
        	tmpPreferences.put(1, preferencesPerGroup.get(1));
        }
    	preferencesPerGroup.clear();
    	preferencesPerGroup = new LinkedHashMap<Integer, Double>(tmpPreferences);
        //Group trips by mode of transport:
        //environmental friendly order: walk, bike, bar, bta, pt, par, car
        LinkedHashMap<String, ArrayList<HashMap<JsonTrip, Double>>> groupedTrips = 
        		new LinkedHashMap<String, ArrayList<HashMap<JsonTrip, Double>>>();
        
        log.debug("The preferencesPerGroup size is: " + preferencesPerGroup.size());
        
        
        for(Map.Entry<Integer, Double> entry : preferencesPerGroup.entrySet()){
        	if (entry.getKey() == 4){
        		groupedTrips.put("walk", new ArrayList<HashMap<JsonTrip, Double>>());
        	}
        	else if (entry.getKey() == 3){        		
                groupedTrips.put("bike", new ArrayList<HashMap<JsonTrip, Double>>());
                groupedTrips.put("bta", new ArrayList<HashMap<JsonTrip, Double>>());
                groupedTrips.put("bar", new ArrayList<HashMap<JsonTrip, Double>>());
        	}
        	else if (entry.getKey() == 2){
        		groupedTrips.put("pt", new ArrayList<HashMap<JsonTrip, Double>>());
        	}
        	else if (entry.getKey() == 1){
        		groupedTrips.put("par", new ArrayList<HashMap<JsonTrip, Double>>());
        		groupedTrips.put("car", new ArrayList<HashMap<JsonTrip, Double>>());
        	}
        }
         
        //groupedTrips.put("walk", new ArrayList<HashMap<JsonTrip, Double>>());
//        groupedTrips.put("bike", new ArrayList<HashMap<JsonTrip, Double>>());
//        groupedTrips.put("bta", new ArrayList<HashMap<JsonTrip, Double>>());
//        groupedTrips.put("bar", new ArrayList<HashMap<JsonTrip, Double>>());
        
        //pt and par_pt should be merged!
        //groupedTrips.put("pt", new ArrayList<HashMap<JsonTrip, Double>>());
        //groupedTrips.put("par_pt", new ArrayList<HashMap<JsonTrip, Double>>());
        //groupedTrips.put("pt", new ArrayList<HashMap<JsonTrip, Double>>());
        //groupedTrips.put("par", new ArrayList<HashMap<JsonTrip, Double>>());
        //groupedTrips.put("car", new ArrayList<HashMap<JsonTrip, Double>>());
        
        for (Iterator<Map.Entry<Integer, HashMap<JsonTrip, Double>>> mapIt = finalTripResults.entrySet().iterator(); mapIt.hasNext();) {
        	Map.Entry<Integer, HashMap<JsonTrip, Double>> entry = mapIt.next();
        	String modality = "";
        	
        	modality = entry.getValue().entrySet().iterator().next().getKey().getModality();
        	if ( modality.matches("par_pt")){
        		modality = "pt";
        	}
        	if ( modality.matches("par_car")){
        		modality = "par";
        	}
        	if (groupedTrips.containsKey(modality)){
        		groupedTrips.get(modality).add(entry.getValue());        	
        	}
        	else{
        		ArrayList<HashMap<JsonTrip, Double>> tripsArray = new ArrayList<HashMap<JsonTrip, Double>>();
        		tripsArray.add(entry.getValue());
        		groupedTrips.put(modality, tripsArray);
        	}
        }
        
        log.warn("Printing some statistics:");
        log.warn("number of different modalities: " + groupedTrips.size());
        for (Iterator<Map.Entry<String, ArrayList<HashMap<JsonTrip, Double>>>> mapIt = groupedTrips.entrySet().iterator(); mapIt.hasNext();) {
        	Map.Entry<String, ArrayList<HashMap<JsonTrip, Double>>> entry = mapIt.next();
        	log.warn("found modality: " + entry.getKey() +
        			" with number of trips: " + entry.getValue().size());        	
        }
        
        //sort finalTripResults based on utility value // not used for now
        //changing idea: sort trips within the mode of transport
        if (false){
	        List<Map.Entry<Integer, HashMap<JsonTrip, Double>>> intermediaryEntries =
	    		  new ArrayList<Map.Entry<Integer, HashMap<JsonTrip, Double>>>(finalTripResults.entrySet());
	    		Collections.sort(intermediaryEntries, new Comparator<Map.Entry<Integer, HashMap<JsonTrip, Double>>>() {
	    		  public int compare(Map.Entry<Integer, HashMap<JsonTrip, Double>> a, Map.Entry<Integer, HashMap<JsonTrip, Double>> b){
	    			  Double aValue = ((Double)(((Map.Entry<JsonTrip,Double>)(a.getValue().entrySet().iterator()
	      		    		.next())).getValue()));
	    			  Double bValue = ((Double)(((Map.Entry<JsonTrip,Double>)(b.getValue().entrySet().iterator()
	        		    		.next())).getValue()));
	    			  return bValue.compareTo(aValue);
	    		  }
	    		});
	    		finalTripResults.clear();
	    		int position = 0;
	    		for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : intermediaryEntries) {
	    			//((JsonTrip)(entry.getValue().entrySet().iterator().next().getKey())).addAttribute(AttributeListKeys.KEY_TRIP_INDEX, Integer.toString(position));
	    			finalTripResults.put(position, entry.getValue());//(entry.getKey(), entry.getValue());
	    			position++;
	    		}
        }
        
        //Re-arrange groups to correspond to user request
        //LinkedHashMap<String, ArrayList<HashMap<JsonTrip, Double>>> groupedTrips
        finalTripResults.clear();
        int position = 0;
        int omittedPosition = 0;
        int maxTripsToShow = 1;
        //check the difference between max Co2 for car and pt
        double MaxCo2ForCar = 0.0;
        double MaxCo2ForPT = 0.0;
        
        int MaxDurationForCar = 0;
        int MaxDurationForPT = 0;
       
       
        for (Iterator<Map.Entry<String, ArrayList<HashMap<JsonTrip, Double>>>> mapIt = groupedTrips.
        		entrySet().iterator(); mapIt.hasNext();) {
        	
        	
        	Map.Entry<String, ArrayList<HashMap<JsonTrip, Double>>> entry = mapIt.next();
        	
        	maxTripsToShow = 1;
        	if (entry.getKey().matches("pt")){
        		maxTripsToShow = this.getMaxPTroutesToShow();
        		log.warn("set to show " + this.getMaxPTroutesToShow() + " pt trips");
        	}
        	
        	Collections.sort(entry.getValue(), new Comparator<HashMap<JsonTrip, Double>>() {
	    		  public int compare(HashMap<JsonTrip, Double> a, HashMap<JsonTrip, Double> b){
	    			  Double aValue = ((Double)(((Map.Entry<JsonTrip,Double>)(a.entrySet().iterator()
	      		    		.next())).getValue()));
	    			  Double bValue = ((Double)(((Map.Entry<JsonTrip,Double>)(b.entrySet().iterator()
	        		    		.next())).getValue()));
	    			  return bValue.compareTo(aValue);
	    		  }
	    		});
        	
        	int minChanges = 100;
        	int maxChanges = 0;
        	int minTime = 1000;
        	int maxTime = 0;
        	int preferredNbrOfChanges = 0;
        	int nbrOfTripsWithPreferredChanges = 0; 
        	
        	
        	
        	if (entry.getKey().matches("pt")){
	        	//find some statistics for the pt mode
        		//these should be already calculated - double check please
	        	
        		try{
        			preferredNbrOfChanges = routeRequest.getOptionsRoute().getPtMaxChanges();
        			log.warn("preferredNbrOfChanges: " + preferredNbrOfChanges);
            	}catch(Exception e){
            		log.warn("could not find prefered number of changes for pt");
            	}
        		
	        	for (HashMap<JsonTrip, Double> arrayEntry : entry.getValue()){
	        		JsonTrip tmpTrip = arrayEntry.entrySet().iterator().next().getKey();
	        		int tripNbrChanges = getNbrOfChanges(tmpTrip);
	        		if (tmpTrip.getDurationMinutes() > maxTime){
	        			maxTime = tmpTrip.getDurationMinutes();
	        		}
	        		if (tmpTrip.getDurationMinutes() < minTime){
	        			minTime = tmpTrip.getDurationMinutes();
	        		}
	        		if (tripNbrChanges > maxChanges){
	        			maxChanges = tripNbrChanges;
	        		}
	        		if (tripNbrChanges < minChanges){
	        			minChanges = tripNbrChanges;
	        		}
	        		
	        		if (preferredNbrOfChanges > 0 && tripNbrChanges <= preferredNbrOfChanges){
	        			nbrOfTripsWithPreferredChanges++;
	        		}
	        			        		
	        	}
        	}
        	
        	
        	//remove some entries
        	//and group by utility 
        	LinkedHashMap<Double, ArrayList<HashMap<JsonTrip, Double>>> tripsGroupedByUtility = new LinkedHashMap<Double, ArrayList<HashMap<JsonTrip, Double>>>();
        	for (HashMap<JsonTrip, Double> arrayEntry : entry.getValue()){
        		boolean placeEntry = true;
        		if (entry.getKey().matches("walk")){
        			if (arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > personalizedWalkingTimeThreshold){ //this.getWalkingTimeThreshold()){
        				placeEntry = false;
        				log.warn("ommiting 'walk' based route since its duration is: " +
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() +
        						" and personalizedWalkingTimeThreshold is " + personalizedWalkingTimeThreshold);
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if (extremeConditions 
        					&& arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > this.maxWalkTimeInExtremeConditions){
        				placeEntry = false;
        				log.warn("ommiting 'walk' based route because of extreme conditions: " +        						
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() + 
        						" maxWalkTimeInExtremeConditions: " + maxWalkTimeInExtremeConditions);        						
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if (askedForWalk == false && arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > 30){
        				log.warn("ommiting 'walk' based route because the user didn't ask for it and the duration is: " +        						
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes());        						
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else{
        				RouteInWalkingDistance = true;
        			}
        		}
        		if (entry.getKey().matches("bike")){
        			if (arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > this.getBikeTimeThreshold()){
        				placeEntry = false;        				
        				log.warn("ommiting 'bike' based route since its duration is: " +
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes());
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if (extremeConditions &&
        					arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > this.maxBikeTimeInExtremeConditions){
        				placeEntry = false;        				
        				log.warn("ommiting 'bike' based route because of extreme conditions: " +
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() +
        						" maxBikeTimeInExtremeConditions: " + maxBikeTimeInExtremeConditions);
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if (askedForBike == false && arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > 60){
        				log.warn("ommiting 'bike' based route since the user didn;t ask for it and its duration is: " +
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes());
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else{
        				RouteInWalkingDistance = true;
        			}
        		}
        		//some logic on how to re-rank pt options
        		if (entry.getKey().matches("pt")){
        			if (arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() > 
        																(int)(this.getTimeThresholdCutoff()*minValues.get("minPTTotalDuration"))){
        				
        				placeEntry = false;
        				log.warn("ommiting 'pt' based route since its duration is very high compared to the others: " +
        						arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes() + " the min value is: " + 
        						minValues.get("minPTTotalDuration"));
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if (getNbrOfChanges(arrayEntry.entrySet().iterator().next().getKey()) > (int)(this.getPtChangesFactorThreshold()*minChanges)){
        				placeEntry = false;
        				log.warn("ommiting 'pt' based route since it contains too many changes: " +
        						getNbrOfChanges(arrayEntry.entrySet().iterator().next().getKey()) +
        						" this.getPtChangesFactorThreshold(): " + this.getPtChangesFactorThreshold() +
        						" minChanges: " + minChanges);
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if (minWBvalue < walkBikeThresholdForOmmitingCarParRoutes){
        				placeEntry = false;
        				log.warn("ommiting 'pt' based route since the destination is in walking distance: " +
        						minWBvalue);
        						//arrayEntry.entrySet().iterator().next().getKey().getSegments().size());
        				RouteInWalkingDistance = true;
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        			else if(preferredNbrOfChanges > 0 && nbrOfTripsWithPreferredChanges > 0
        					&& getNbrOfChanges(arrayEntry.entrySet().iterator().next().getKey()) > preferredNbrOfChanges){
        				placeEntry = false;
        				log.warn("ommiting 'pt' based route since it contains too many changes compared to the preferred: " +
        						getNbrOfChanges(arrayEntry.entrySet().iterator().next().getKey()) +
        						" preferredNbrOfChanges: " + preferredNbrOfChanges);
        				omittedTripResults.put(omittedPosition, arrayEntry);
        				omittedPosition++;
        			}
        		}
        		
        		if ((entry.getKey().matches("car") || entry.getKey().matches("par")) && (minWBvalue < walkBikeThresholdForOmmitingCarParRoutes)){
        			placeEntry = false;
    				log.warn("ommiting " + entry.getKey() + " based route since the destination is in walking distance: " +
    						minWBvalue);
    				RouteInWalkingDistance = true;
    						//arrayEntry.entrySet().iterator().next().getKey().getSegments().size());
    				omittedTripResults.put(omittedPosition, arrayEntry);
    				omittedPosition++;
        		}
        		
        		if (entry.getKey().matches("par")){
        			//get car time
        			try{
	        			double carTime = 0.0;
	        			double totalTripTime = 0.0;
	        			for(JsonSegment segment : arrayEntry.entrySet().iterator().next().getKey().getSegments()){	        				
	        				if (segment.getType().matches("car")){
	        					carTime += (double)segment.getDurationMinutes();
	        				}
	        				totalTripTime += (double)segment.getDurationMinutes();
	        			}
	        			log.warn("total time for par trip: " + totalTripTime +
	        					" total time of the car segments: " + carTime);
	        			if (carTime > this.getThresholdForParkAndRide()*totalTripTime){
	        				placeEntry = false;
	        				log.warn("ommiting 'par' based route since it doens't make sense: ");
	        				omittedTripResults.put(omittedPosition, arrayEntry);
	        				omittedPosition++;
	        			}
        			}
        			catch(Exception ex1){
        				ex1.printStackTrace();
        			}
        		}
        		
        		if (placeEntry){
        			if (tripsGroupedByUtility.containsKey(arrayEntry.entrySet().iterator().next().getValue())){
        				tripsGroupedByUtility.get(arrayEntry.entrySet().iterator().next().getValue()).add(arrayEntry);
        			}
        			else{
        				ArrayList<HashMap<JsonTrip, Double>> array = new ArrayList<HashMap<JsonTrip, Double>>();
        				array.add(arrayEntry);
        				tripsGroupedByUtility.put(arrayEntry.entrySet().iterator().next().getValue(), array);
        			}
        			
        		}
        	}
        	
        	log.warn("found " + tripsGroupedByUtility.size() + " utility groups");
        	
        	for (Map.Entry<Double, ArrayList<HashMap<JsonTrip, Double>>> 
        		tripsGroupedByUtilityEntry : tripsGroupedByUtility.entrySet()){
        		log.warn("utility group size: " + tripsGroupedByUtilityEntry.getValue().size());
        		if (tripsGroupedByUtilityEntry.getValue().size() > 1){
        			ArrayList<HashMap<JsonTrip, Double>> tmpArrayEmissions = new ArrayList<HashMap<JsonTrip, Double>> (tripsGroupedByUtilityEntry.getValue());
        			ArrayList<HashMap<JsonTrip, Double>> tmpArrayDuration = new ArrayList<HashMap<JsonTrip, Double>> (tripsGroupedByUtilityEntry.getValue());
        			ArrayList<HashMap<JsonTrip, Double>> tmpArrayChanges = new ArrayList<HashMap<JsonTrip, Double>> (tripsGroupedByUtilityEntry.getValue());
        			//this case means that we have multiple entries with the same utility value!
        			// some should be removed in order not to overload the user with information.
        			// our target is to provide ~3 options
        			
        			//first sort by least emissions 
        			Collections.sort(tmpArrayEmissions, new Comparator<HashMap<JsonTrip, Double>>() {
      	    		  public int compare(HashMap<JsonTrip, Double> a, HashMap<JsonTrip, Double> b){
      	    			  Double aValue = ((Double)Double.parseDouble((((Map.Entry<JsonTrip,Double>)(a.entrySet().iterator()
      	      		    		.next())).getKey()).getAttribute(AttributeListKeys.KEY_SEGMENT_CO2)));      	    			
      	    			  Double bValue = ((Double)Double.parseDouble((((Map.Entry<JsonTrip,Double>)(b.entrySet().iterator()
        	      		    		.next())).getKey()).getAttribute(AttributeListKeys.KEY_SEGMENT_CO2)));
      	    			  return aValue.compareTo(bValue);
      	    		  }
      	    		});
        			
        			//then the one with the least duration
        			Collections.sort(tmpArrayDuration, new Comparator<HashMap<JsonTrip, Double>>() {
        	    		  public int compare(HashMap<JsonTrip, Double> a, HashMap<JsonTrip, Double> b){
        	    			  Double aValue = ((double)((((Map.Entry<JsonTrip,Double>)(a.entrySet().iterator()
        	      		    		.next())).getKey()).getDurationMinutes()));      	    			
        	    			  Double bValue = ((double)((((Map.Entry<JsonTrip,Double>)(b.entrySet().iterator()
          	      		    		.next())).getKey()).getDurationMinutes()));
        	    			  return aValue.compareTo(bValue);
        	    		  }
        	    		});
        			
        			//finally the one with the least changes
        			Collections.sort(tmpArrayChanges, new Comparator<HashMap<JsonTrip, Double>>() {
      	    		  public int compare(HashMap<JsonTrip, Double> a, HashMap<JsonTrip, Double> b){
      	    			  Double aValue = ((double)((((Map.Entry<JsonTrip,Double>)(a.entrySet().iterator()
      	      		    		.next())).getKey()).getSegments().size()));      	    			
      	    			  Double bValue = ((double)((((Map.Entry<JsonTrip,Double>)(b.entrySet().iterator()
        	      		    		.next())).getKey()).getSegments().size()));
      	    			  return aValue.compareTo(bValue);
      	    		  }
      	    		});
        			
        			//finally get the score of each option 
        			ArrayList<HashMap<Integer, Integer>> scores = new ArrayList<HashMap<Integer, Integer>>();
        			int scoresIterator = 0;
        			for (HashMap<JsonTrip, Double> tmpEntry : tripsGroupedByUtilityEntry.getValue()){
        				HashMap<Integer, Integer> score = new HashMap<Integer, Integer>(); 
        				score.put(scoresIterator, tmpArrayEmissions.indexOf(tmpEntry) +
        						tmpArrayDuration.indexOf(tmpEntry) +
        						tmpArrayChanges.indexOf(tmpEntry));
        				scores.add(score);
        				scoresIterator++;
        			}
        			Collections.sort(scores, new Comparator<HashMap<Integer, Integer>>() {
        	    		  public int compare(HashMap<Integer, Integer> a, HashMap<Integer, Integer> b){
        	    			  Double aValue = ((double)((((Map.Entry<Integer, Integer>)(a.entrySet().iterator()
        	      		    		.next())).getValue())));      	    			
        	    			  Double bValue = ((double)((((Map.Entry<Integer, Integer>)(b.entrySet().iterator()
          	      		    		.next())).getValue())));
        	    			  return aValue.compareTo(bValue);
        	    		  }
        	    		});
        			
        			//HashMap<JsonTrip, Double> tripToKeep = tripsGroupedByUtilityEntry.getValue().
        			//		get(scores.get(0).entrySet().iterator().next().getKey());
        			// decision to add two trips - I don't know if its a good or a bad one :)
        			HashMap<JsonTrip, Double> trip1ToKeep = new HashMap<JsonTrip, Double>(tripsGroupedByUtilityEntry.getValue().
                					get(scores.get(0).entrySet().iterator().next().getKey()));
        			HashMap<JsonTrip, Double> trip2ToKeep = new HashMap<JsonTrip, Double>(tripsGroupedByUtilityEntry.getValue().
        					get(scores.get(1).entrySet().iterator().next().getKey()));
        			//we are keeping two trips
        			for(int i = 2; i< scores.size(); i++){
        				omittedTripResults.put(omittedPosition, tripsGroupedByUtilityEntry.getValue().
            					get(scores.get(i).entrySet().iterator().next().getKey()));
        				omittedPosition++;
        			}
        			
        			tripsGroupedByUtilityEntry.getValue().clear();
        			tripsGroupedByUtilityEntry.getValue().add(trip1ToKeep);
        			log.warn("trip1ToKeep: " + trip1ToKeep.entrySet().iterator().next().getKey().toString()); 
        			Reports reports = new Reports();
        			if (!reports.printTripInfo(trip1ToKeep.entrySet().iterator().next().getKey())
        					.matches(reports.printTripInfo(trip2ToKeep.entrySet().iterator().next().getKey()))){
        				tripsGroupedByUtilityEntry.getValue().add(trip2ToKeep);
        				log.warn("trip1ToKeep: " + trip2ToKeep.entrySet().iterator().next().getKey().toString());
        			}else{
        				log.warn("found duplicate!!!");
        			}
    			
        			log.warn("ommiting some duplicate trips");
        			
        		}
        		 
        		log.warn("adding trips for modality: " + entry.getKey() + " with size: " + tripsGroupedByUtilityEntry.getValue().size());        		
        		for(HashMap<JsonTrip, Double> arrayEntry : tripsGroupedByUtilityEntry.getValue()){
        			if (maxTripsToShow > 0){
        				if (((JsonTrip)arrayEntry.entrySet().iterator().next().getKey()).getModality().matches("pt") && this.showMessageForPT){
        					
        					//((JsonTrip)arrayEntry.entrySet().iterator().next().getKey()).addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC, 
        					//		this.getMessageForPT());
        					//log.warn("added MessageForPT: " + this.getMessageForPT());
        				}
        				else if (((JsonTrip)arrayEntry.entrySet().iterator().next().getKey()).getModality().matches("walk") && this.showMessageForWalk){
        					//String message = messages.getMessageForWalk();
        					//((JsonTrip)arrayEntry.entrySet().iterator().next().getKey()).addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC, 
        					//		message);//this.getMessageForWalk());
        					//log.warn("added MessageForWalk: " + message);//this.getMessageForWalk());
        				}
        				else if (((JsonTrip)arrayEntry.entrySet().iterator().next().getKey()).getModality().matches("car")){
        					//String message = messages.getMessageForCar();
        					//((JsonTrip)arrayEntry.entrySet().iterator().next().getKey()).addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_DESC, 
        					//		message);//this.getMessageForWalk());
        					//log.warn("added MessageForCar: " + message);//this.getMessageForWalk());
        				}
        				
	            		finalTripResults.put(position, arrayEntry);//(entry.getKey(), entry.getValue());
	    	        	position++;
	    	        	log.warn("adding new trip with modality: " + arrayEntry.entrySet().iterator().next().getKey().getModality());
	    	        	if (entry.getKey().matches("pt")){
	    	        		log.warn("checking CO2 for pt trip");
	    	        		double currentTripCo2 = Double.parseDouble(arrayEntry.entrySet().iterator().next().getKey().getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
	    	        		if (currentTripCo2 > MaxCo2ForPT){
	    	        			MaxCo2ForPT = currentTripCo2; 	    	        			
	    	        		}
	    	        		log.warn("checking duration for pt trip");
	    	        		int currentTripDuration = arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes();
	    	        		if (currentTripDuration > MaxDurationForPT){
	    	        			MaxDurationForPT = currentTripDuration; 	    	        			
	    	        		}
	    	        	}
	    	        	else if (entry.getKey().matches("car")){
	    	        		log.warn("checking CO2 for car trip");
	    	        		double currentTripCo2 = Double.parseDouble(arrayEntry.entrySet().iterator().next().getKey().getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
	    	        		if (currentTripCo2 > MaxCo2ForCar){
	    	        			MaxCo2ForCar = currentTripCo2; 	    	        			
	    	        		}
	    	        		log.warn("checking duration for car trip");
	    	        		int currentTripDuration = arrayEntry.entrySet().iterator().next().getKey().getDurationMinutes();
	    	        		if (currentTripDuration > MaxDurationForCar){
	    	        			MaxDurationForCar = currentTripDuration;
	    	        		}
	    	        	}
        			}
        			maxTripsToShow--;
        		}

        	}
        	
        }
        
        log.warn("MaxCo2ForPT: " + MaxCo2ForPT);
        log.warn("MaxCo2ForCar: " + MaxCo2ForCar);
        
        if (MaxCo2ForCar > 2*MaxCo2ForPT){
        	HighEmmissionsDifferenceBetweenCarAndPT = true;
        }
        
        log.warn("MaxDurationForPT: " + MaxDurationForPT);
        log.warn("MaxDurationForCar: " + MaxDurationForCar);
        
        int difference = Math.abs(MaxDurationForCar - MaxDurationForPT);
        
        if (difference == 0){
        	ComparableCarAndPTRouteDuration = true;
        }
        //should be parameterized
        else if (difference != 0 && difference /((MaxDurationForCar + MaxDurationForPT)/2) < 0.1 ){
        	ComparableCarAndPTRouteDuration = true;
        }
        else{
        	//do nothing?
        }
        
        //add all omitted trips to get an overview
        if (SHOWALL){
	        for(Map.Entry<Integer, HashMap<JsonTrip, Double>> omittedTrip : omittedTripResults.entrySet()){
	        	finalTripResults.put(position, omittedTrip.getValue());
	        	position++;
	        }
        }

        //Iterate routeResults and add the remainding results to the final routes
        //temporarily skip this
//        for (int temp = 0; temp < routeResults.size(); temp++){
//            if (!topListValues.contains(temp)){
//            	JsonTrip trip = (JsonTrip) ((JsonTrip) (tripsList.get(temp)));
//                HashMap tripWithUtility = new HashMap<JsonTrip, Double>();
//                tripWithUtility.put(trip, tripsAndUtilities.get(trip));
//                finalTripResults.put(tripCounter,tripWithUtility);               
//                tripCounter++;
//            }
//        }
        log.warn("after finding the route");
             
//        //Print results to check if we have UTF-8
//        Collection c = finalTripResults.values();
//        Iterator itr = c.iterator();
//        PrintWriter outUTF8 = new PrintWriter(new OutputStreamWriter(System.out));
//        int ci = 1;
        //Messages messages = (Messages) appContext.getBean("Messages");
       
        List<UserTreeScores> userTreeScoreList = userTreeScoresService.findAllUserTreeScores();
        UserTreeScores userTreeScore = userTreeScoresService.findUserTreeScore(userId);
        if (userTreeScoreList != null && userTreeScore != null){
        	double[] scoresArray = new double[userTreeScoreList.size()];
        	int i = 0;
        	for (UserTreeScores score : userTreeScoreList){
        		scoresArray[i] = score.getScore();
        		i++;
        	}
        	double median;
        	if (scoresArray.length % 2 == 0)
        	    median = ((double)scoresArray[scoresArray.length/2] + (double)scoresArray[scoresArray.length/2 - 1])/2;
        	else
        	    median = (double) scoresArray[scoresArray.length/2];
        	if (userTreeScore.getScore() > median){
        		EmmissionsHighComparedToOtherUsers = true;
        		log.warn("user Tree score compared to median is higher");
        	}
        }
        
        TreeScore treeScore = 
				(TreeScore) appContext.getBean("TreeScore");
        int emmissionsTrend = treeScore.getEmmissionsTrend(userId);
        if (emmissionsTrend == 1){
        	EmmissionsIncreasing = true;
        }
        
        //need to rearrange the trip results so we that we have the lowest
        LinkedHashMap<String, LinkedHashMap <Integer, HashMap<JsonTrip, Double>>> groupedFinalTripResults =
        		new LinkedHashMap<String, LinkedHashMap <Integer, HashMap<JsonTrip, Double>>>();
        for (Map.Entry<Integer, HashMap<JsonTrip, Double>> entry : finalTripResults.entrySet()){
        	if (!groupedFinalTripResults.containsKey(entry.getValue().entrySet().iterator().next().getKey().getModality())){
        		LinkedHashMap <Integer, HashMap<JsonTrip, Double>> tmp = new LinkedHashMap <Integer, HashMap<JsonTrip, Double>>();
        		tmp.put(entry.getKey(), entry.getValue());
        		groupedFinalTripResults.put(entry.getValue().entrySet().iterator().next().getKey().getModality(),
        				tmp);
        	}
        	else{
        		LinkedHashMap <Integer, HashMap<JsonTrip, Double>> tmp = groupedFinalTripResults.get(
        				entry.getValue().entrySet().iterator().next().getKey().getModality());
        		tmp.put(entry.getKey(), entry.getValue());
        		groupedFinalTripResults.put(entry.getValue().entrySet().iterator().next().getKey().getModality(),
        				tmp);
        	}
        }
        finalTripResults.clear();
        for (Map.Entry<String, LinkedHashMap <Integer, HashMap<JsonTrip, Double>>> entry : 
        		groupedFinalTripResults.entrySet()){
        	LinkedHashMap <Integer, HashMap<JsonTrip, Double>> tmp = entry.getValue();
        	List<Map.Entry<Integer, HashMap<JsonTrip, Double>>> entries =
        			  new ArrayList<Map.Entry<Integer, HashMap<JsonTrip, Double>>>(tmp.entrySet());
        	Collections.sort(entries, new Comparator<Map.Entry<Integer, HashMap<JsonTrip, Double>>>() {
	    		  public int compare(Map.Entry<Integer, HashMap<JsonTrip, Double>> a, Map.Entry<Integer, HashMap<JsonTrip, Double>> b){
	    			  Double aValue = ((double) Double.parseDouble(a.getValue().entrySet().iterator().
	    					  next().getKey().getAttribute(AttributeListKeys.KEY_SEGMENT_CO2)));      	    			
	    			  Double bValue = ((double) Double.parseDouble(b.getValue().entrySet().iterator().
	    					  next().getKey().getAttribute(AttributeListKeys.KEY_SEGMENT_CO2)));
	    			  return aValue.compareTo(bValue);
	    		  }
	    		});
        	for (Map.Entry<Integer, HashMap<JsonTrip, Double>> sortedEntry : entries) {
        		finalTripResults.put(sortedEntry.getKey(), sortedEntry.getValue());
        		JsonTrip tmpTrip = sortedEntry.getValue().entrySet().iterator().next().getKey();
        		
        		if (modalitiesInFinalResults.containsKey(tmpTrip.getModality())){
        			int tmpValue = modalitiesInFinalResults.get(tmpTrip.getModality());
        			tmpValue++;
        			modalitiesInFinalResults.put(tmpTrip.getModality(), tmpValue);
        		}
        		else{
        			modalitiesInFinalResults.put(tmpTrip.getModality(), 1);
        		}
        	}
        }
        
        return finalTripResults;
  }
   
  private LinkedHashMap methodForRecommendations2(UserPreferences userPreferences, 
		  ArrayList<JsonResponseRoute> routeResults, String city, UserVehicle userVehicle){
        
	  log.warn("methodForRecommendations2");
	  
	  	ArrayList tripsList = new ArrayList<JsonTrip>();
  	
  		for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
    		log.warn("emissions: " + trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
    		tripsList.add(trip);
    	  }
    	}
	  
        //ArrayList<String> myArr = new ArrayList<String>();
        LinkedHashMap resultsAndUtilities = new LinkedHashMap<JsonTrip, Double>();
        
        Iterator iterator = tripsList.iterator();
        while ( iterator.hasNext() ){
        	JsonTrip tripResult = (JsonTrip) iterator.next();                        
        
            double howMany = 0.0;
            try{ 
                boolean writeToFile = true;
                String osName = System.getProperty("os.name");
                if (!osName.matches("(?i:.*windows.*)")){
                    writeToFile = true;
                }

                double utility = 0.0;
                ArrayList<Integer> myArr = new ArrayList<Integer>();

                double totalDuration = 0.0;

                switch(((Double)userPreferences.getUtilityAlgorithm()).intValue()){
                    case 0: utility += routeUtilityCalulation(tripResult, userPreferences);
                        break;
                    case 1: utility += routeUtilityCalulationP1(tripResult, userPreferences);
                        break;
                    case 2: utility += routeUtilityCalulationP2(tripResult, userPreferences);
                        break;
                    case 3: utility += routeUtilityCalulationP3(tripResult, userPreferences);
                        break;
                    case 4: utility += routeUtilityCalulationP4(tripResult, userPreferences, city, null);
                        break;                                        
                    default: utility += routeUtilityCalulation(tripResult, userPreferences);
                        break;                                        
                }
                //utility += routeUtilityCalulation(routeResult, userPreferences);
                totalDuration += (double)this.getTripTotalDuration(tripResult);
                    
                resultsAndUtilities.put(tripResult, utility);
                howMany++;
            }
            catch(Exception e){
                    e.printStackTrace();	
            }
        }	
        
        System.out.println("number of routes fetched: " + resultsAndUtilities.size());

        LinkedHashMap finalTripResults = new LinkedHashMap<Integer, JsonTrip>();

        LinkedHashMap tmpFinalTripResults = null; //GetRoutes.sortByValue(resultsAndUtilities);
        Iterator iterator1 = tmpFinalTripResults.keySet().iterator();
        int position = 0;
        
        System.out.println("++++++ Going to Print Ordered Utilities: ++++++");
        System.out.println(" resultsAndUtilities size: " + resultsAndUtilities.size());
        System.out.println("tmpFinalRouteResults size: " + tmpFinalTripResults.size());
        while ( iterator1.hasNext() ){
        	JsonResponseRoute key = ( JsonResponseRoute ) iterator1.next();
            finalTripResults.put(position, key);
            System.out.println("position: " + position + " utility: " + (Double)tmpFinalTripResults.get(key)
                    + " totalDuration: " + "key.getTotalDuration()" 
                    + " wbDuration: " + "key.getWBDuration()");
            position++;
        }
        
      return finalTripResults;
  }
  
   public double routeUtilityCalulation(JsonTrip tripResult, UserPreferences userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.getDuration10min();
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		durationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
	
	double wbDuration = (double) getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.getWB10min();
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.getWB30min();
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.getWB30plus();
	}
		
	totalUtility = (1/totalDuration)* 
					durationCriterionValue*
					((Double)userPreferences.getDurationImportance()) 
					+ 
					(1/wbDuration)* 
					wbDurationCriterionValue*
					((Double)userPreferences.getWbtimeImportance());
	
	return totalUtility;
  }

  public double routeUtilityCalulationP1(JsonTrip tripResult, UserPreferences userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.getDuration10min();
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		durationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
	
	double wbDuration = (double) getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.getWB10min();
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.getWB30min();
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.getWB30plus();
	}
		
	totalUtility = (totalDuration/(Double)maxValues.get("maxTotalDuration"))* 
						durationCriterionValue*
						((Double)userPreferences.getDurationImportance()) 
						+ 
						(wbDuration/(Double)maxValues.get("maxWBTotalDuration"))* 
						wbDurationCriterionValue*
						((Double)userPreferences.getWbtimeImportance());
	
	return totalUtility;
  }
  
  public double routeUtilityCalulationP2(JsonTrip tripResult, UserPreferences userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.getDuration10min();
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		durationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.getWB10min();
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.getWB30min();
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.getWB30plus();
	}
		
	totalUtility = ((totalDuration - (Double)minValues.get("minTotalDuration"))/
                                    ((Double)maxValues.get("maxTotalDuration") - (Double)minValues.get("minTotalDuration")))*
                                                durationCriterionValue*
						((Double)userPreferences.getDurationImportance()) 
						+ 
						((wbDuration - (Double)minValues.get("minWBTotalDuration"))/
                                    ((Double)maxValues.get("maxWBTotalDuration") - (Double)minValues.get("minWBTotalDuration")))*                                                
						wbDurationCriterionValue*
						((Double)userPreferences.getWbtimeImportance());
	
	return totalUtility;
  }
  
  public double routeUtilityCalulationP3(JsonTrip tripResult, UserPreferences userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.getDuration10min();
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		durationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.getWB10min();
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.getWB10min();
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.getWB30plus();
	}
		
	totalUtility = (totalDuration/(Double)sumValues.get("sumTotalDuration"))* 
						durationCriterionValue*
						((Double)userPreferences.getDurationImportance()) 
						+ 
						(wbDuration/(Double)sumValues.get("sumWBTotalDuration"))* 
						wbDurationCriterionValue*
						((Double)userPreferences.getWbtimeImportance());
	
	return totalUtility;
  }
  
  public double routeUtilityCalulationP4(JsonTrip tripResult, UserPreferences userPreferences, String city, UserVehicle userVehicle){
	double totalUtility = 0;
	
	double totalDuration = (double) this.getTripTotalDuration(tripResult);
        double totalEmissions = this.getTripTotalEmissions(tripResult, city, userVehicle);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.getDuration10min();
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		durationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
        
    int numberOfChanges = tripResult.getSegments().size();
    double comfortCriterionValue = 0;
    
    if (numberOfChanges <= 2){
        comfortCriterionValue = (Double) userPreferences.getComfortHigh();
    }
    else if (numberOfChanges > 2 && numberOfChanges <= 3){
        comfortCriterionValue = (Double) userPreferences.getComfortMedium();
    }
    else {
        comfortCriterionValue = (Double) userPreferences.getComfortLow();
    }
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.getWB10min();
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
		
	totalUtility = durationCriterionValue*
						((Double)userPreferences.getDurationImportance()) 
						+ 
						wbDurationCriterionValue*
						((Double)userPreferences.getWbtimeImportance())
                                                +
                                                comfortCriterionValue*
                				((Double)userPreferences.getComfortImportance())
                                                ;
        
        if (false){
            totalUtility = totalUtility * totalEmissions/((Double)sumValues.get("sumTotalEmissions"));
        }
	return totalUtility;
  }
  
  public double routeUtilityCalulationP5(JsonTrip tripResult, UserPreferences userPreferences, String city, UserVehicle userVehicle){
	double totalUtility = 0;
		
	double totalDuration = (double) this.getTripTotalDuration(tripResult);
    double totalEmissions = this.getTripTotalEmissions(tripResult, city, userVehicle);
    double nominalEmissions = this.getTripNominalEmissions(tripResult);
    
    
    
    System.out.println("trip emissions: " + totalEmissions);
    
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.getDuration10min();
	} 
	else if (10 < totalDuration && totalDuration < 20){
		durationCriterionValue = (Double)userPreferences.getDuration30min();
	}
	else{
		durationCriterionValue = (Double)userPreferences.getDuration30plus();
	}
        
    int numberOfChanges = tripResult.getSegments().size();
    double comfortCriterionValue = 0;
    
    if (numberOfChanges <= 2){
        comfortCriterionValue = (Double) userPreferences.getComfortHigh();
    }
    else if (numberOfChanges > 2 && numberOfChanges <= 3){
        comfortCriterionValue = (Double) userPreferences.getComfortMedium();
    }
    else {
        comfortCriterionValue = (Double) userPreferences.getComfortLow();
    }
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration > 0){
		if (wbDuration <= 10){
			wbDurationCriterionValue = (Double)userPreferences.getWB10min();
		} 
		else if (10 < wbDuration && wbDuration <= 30){
			wbDurationCriterionValue = (Double)userPreferences.getWB30min();
		}
		else{
			wbDurationCriterionValue = (Double)userPreferences.getWB30plus();
		}
	}
	
	double emissionsCriterionValue = 0;
	if (totalEmissions <= 1.0*nominalEmissions){
		emissionsCriterionValue = (Double)userPreferences.getEmissionsLow();
	}
	else if (totalEmissions > 1.0*nominalEmissions && totalEmissions <= 1.6*nominalEmissions){
		emissionsCriterionValue = (Double)userPreferences.getEmissionsMedium();
	}
	else{
		emissionsCriterionValue = (Double)userPreferences.getEmissionsHigh();
	}
		
	totalUtility = durationCriterionValue*
						((Double)userPreferences.getDurationImportance()) 
						+ 
						wbDurationCriterionValue*
						((Double)userPreferences.getWbtimeImportance())
                                                +
                                                comfortCriterionValue*
                				((Double)userPreferences.getComfortImportance())
                                                ;
    
	totalUtility = totalUtility*(1.0-(Double)userPreferences.getEcoAttitudeImportance())
								- emissionsCriterionValue*(Double)userPreferences.getEcoAttitudeImportance();
	return totalUtility;
  }
  
  //routeUtilityCalulationP6
  public double routeUtilityCalulationP6(JsonTrip tripResult, UserPreferences userPreferences, String city, UserVehicle userVehicle){
		
	  	log.warn("*** calculating based on routeUtilityCalulationP6 ***");
	  	double totalUtility = 0;			
		double totalDuration = (double) this.getTripTotalDuration(tripResult);
		log.warn("totalDuration: " + totalDuration);
	    double totalEmissions = this.getTripTotalEmissions(tripResult, city, userVehicle);
	    log.warn("totalEmissions: " + totalEmissions);
	    double nominalEmissions = this.getTripNominalEmissions(tripResult);
	    log.warn("nominalEmissions: " + nominalEmissions);
	    
	    log.warn("trip emissions: " + totalEmissions);
	    
	    //1st approach based on the mean value for total trip time
	    //it's like taking some nominal values for the trip
	    
	    double minTime = minValues.get("minTotalDuration");
	    log.warn("minTime: " + minTime);
	    double maxTime = maxValues.get("maxTotalDuration");
	    log.warn("maxTime: " + maxTime);
	    double meanTime = meanValues.get("meanTotalDuration");
	    log.warn("meanTime: " + meanTime);
	    
	    double intervalPercentage = 0.3; 
	    
		double durationCriterionValue = 0;
		if (minTime <= totalDuration && totalDuration <= (meanTime - meanTime*0.3)){
			durationCriterionValue = (Double)userPreferences.getDuration10min();
		} 
		else if (meanTime - meanTime*0.3 < totalDuration && totalDuration <= (meanTime + meanTime*0.3)){
			durationCriterionValue = (Double)userPreferences.getDuration30min();
		}
		else{
			durationCriterionValue = (Double)userPreferences.getDuration30plus();
		}
	        
		log.warn("durationCriterionValue: " + durationCriterionValue);
		
	    int numberOfChanges = this.getNbrOfChanges(tripResult);
	    log.warn("number of changes: " + numberOfChanges);
	    double comfortCriterionValue = 0;
	    
	    if (numberOfChanges <= 2){
	        comfortCriterionValue = (Double) userPreferences.getComfortHigh();
	    }
	    else if (numberOfChanges > 2 && numberOfChanges <= 3){
	        comfortCriterionValue = (Double) userPreferences.getComfortMedium();
	    }
	    else {
	        comfortCriterionValue = (Double) userPreferences.getComfortLow();
	    }
		
	    log.warn("comfortCriterionValue: " + comfortCriterionValue);
	    
	    //1st approach based on the mean value for total trip time
	    double minWBTime = minValues.get("minWBTotalDuration");
	    double maxWBTime = maxValues.get("maxWBTotalDuration");
	    double meanWBTime = meanValues.get("meanWBTotalDuration");
	    double intervalWBPercentage = 0.3; 
	    
		double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
		double wbDurationCriterionValue = 0;
		if (wbDuration > 0){
			if (minWBTime <= wbDuration && wbDuration <= (meanWBTime - meanWBTime*0.3)){
				wbDurationCriterionValue = (Double)userPreferences.getWB10min();
			} 
			else if ((meanWBTime - meanWBTime*0.3) < wbDuration && wbDuration <= (meanWBTime + meanWBTime*0.3)){
				wbDurationCriterionValue = (Double)userPreferences.getWB30min();
			}
			else{
				wbDurationCriterionValue = (Double)userPreferences.getWB30plus();
			}
		}
		
		log.warn("wbDurationCriterionValue: " + wbDurationCriterionValue);
		
		double emissionsCriterionValue = 0;
		if (totalEmissions <= 1.0*nominalEmissions){
			emissionsCriterionValue = (Double)userPreferences.getEmissionsLow();
		}
		else if (totalEmissions > 1.0*nominalEmissions && totalEmissions <= 1.6*nominalEmissions){
			emissionsCriterionValue = (Double)userPreferences.getEmissionsMedium();
		}
		else{
			emissionsCriterionValue = (Double)userPreferences.getEmissionsHigh();
		}
		
		log.warn("emissionsCriterionValue: " + emissionsCriterionValue);
			
		totalUtility = durationCriterionValue*
							((Double)userPreferences.getDurationImportance()) 
							+ 
							wbDurationCriterionValue*
							((Double)userPreferences.getWbtimeImportance())
	                                                +
	                                                comfortCriterionValue*
	                				((Double)userPreferences.getComfortImportance())
	                                                ;
	    
		totalUtility = totalUtility*(1.0-(Double)userPreferences.getEcoAttitudeImportance())
									- emissionsCriterionValue*(Double)userPreferences.getEcoAttitudeImportance();
		
		log.warn("totalUtility: " + totalUtility);		
		return totalUtility;
	  }
  
  
    private void updateTotalWBDurationStats(ArrayList<JsonResponseRoute> routeResults){
    	ArrayList<Double> durations = new ArrayList<Double>();
    	double numberOfTrips = 0;
    	for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
	          double walkingBicycleDuration = getTripTotalWBDuration(trip);
	          durations.add(walkingBicycleDuration);
	          if (maxValues.containsKey("maxWBTotalDuration")){
	            double currentMaxTotalDuration = (Double) maxValues.get("maxWBTotalDuration");
	            if (currentMaxTotalDuration < walkingBicycleDuration){
	                maxValues.put("maxWBTotalDuration", walkingBicycleDuration);
	            }
	          }
	          else{
	              maxValues.put("maxWBTotalDuration", walkingBicycleDuration);
	          }
	
	          if (minValues.containsKey("minWBTotalDuration")){
	            double currentMinTotalDuration = (Double) minValues.get("minWBTotalDuration");
	            if (currentMinTotalDuration > walkingBicycleDuration){
	                minValues.put("minWBTotalDuration", walkingBicycleDuration);
	            }
	          }
	          else{
	              minValues.put("minWBTotalDuration", walkingBicycleDuration);
	          }
	
	          if (sumValues.containsKey("sumWBTotalDuration")){
	            double sumTotalDuration = (Double) sumValues.get("sumWBTotalDuration");
	            sumTotalDuration += walkingBicycleDuration;
	            sumValues.put("sumWBTotalDuration", sumTotalDuration);
	          }
	          else{
	              sumValues.put("sumWBTotalDuration", walkingBicycleDuration);
	          }
	          numberOfTrips++;
    	  }
      }
    	log.warn("numberOfTrips: " + numberOfTrips);
        log.warn("mean numberOfTrips: " + (int)numberOfTrips/2);
        Collections.sort(durations);
        int middle = ((durations.size()) / 2);
        if(durations.size() % 2 == 0){
  		   double medianA = durations.get(middle);
  		   double medianB = durations.get(middle+1);
  		   medianValues.put("medianWBTotalDuration", (medianA + medianB) / 2);
        } else{
      	  medianValues.put("medianWBTotalDuration", durations.get(middle+1));       
        }
        meanValues.put("meanWBTotalDuration", sumValues.get("sumWBTotalDuration")/numberOfTrips); 
  }
  
  private void updateTotalDurationStats(ArrayList<JsonResponseRoute> routeResults){ 
	  double numberOfTrips = 0;
	  ArrayList<Double> durations = new ArrayList<Double>();
      for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
	        double totalDuration = getTripTotalDuration2(trip); // 2 for new version of total duration
	        durations.add(totalDuration);
	        if (maxValues.containsKey("maxTotalDuration")){
	            double currentMaxTotalDuration = (Double) maxValues.get("maxTotalDuration");
	            if (currentMaxTotalDuration < totalDuration){
	                maxValues.put("maxTotalDuration", totalDuration);
	            }
	        }
	        else{
	            maxValues.put("maxTotalDuration", totalDuration);
	        }
	
	        if (minValues.containsKey("minTotalDuration")){
	          double currentMinTotalDuration = (Double) minValues.get("minTotalDuration");
	          if (currentMinTotalDuration > totalDuration){
	              minValues.put("minTotalDuration", totalDuration);
	          }
	        }
	        else{
	            minValues.put("minTotalDuration", totalDuration);
	        }
	
	        if (sumValues.containsKey("sumTotalDuration")){
	          double sumTotalDuration = (Double) sumValues.get("sumTotalDuration");
	          sumTotalDuration += totalDuration;
	          sumValues.put("sumTotalDuration", sumTotalDuration);
	        }
	        else{
	            sumValues.put("sumTotalDuration", totalDuration);
	        }
	        
	        if (trip.getModality().matches("pt") || trip.getModality().matches("pt")){
	        	log.warn("updating duration statistics: found pt trip with duration: " + totalDuration);
	        	if (maxValues.containsKey("maxPTTotalDuration")){
		            double currentMaxPTTotalDuration = (Double) maxValues.get("maxPTTotalDuration");
		            if (currentMaxPTTotalDuration < totalDuration){
		                maxValues.put("maxPTTotalDuration", totalDuration);
		            }
		        }
		        else{
		            maxValues.put("maxPTTotalDuration", totalDuration);
		        }
	        	
	        	if (minValues.containsKey("minPTTotalDuration")){
	  	          double currentMinPTTotalDuration = (Double) minValues.get("minPTTotalDuration");
	  	          if (currentMinPTTotalDuration > totalDuration){
	  	              minValues.put("minPTTotalDuration", totalDuration);
	  	          }
	  	        }
	  	        else{
	  	            minValues.put("minPTTotalDuration", totalDuration);
	  	        }
	        }
	        
	        numberOfTrips++;
	        
	        if (trip.getModality().matches("walk") || trip.getModality().matches("bike")){
	        	if ((double)trip.getDurationMinutes()<minWBvalue){
	        		minWBvalue = (double)trip.getDurationMinutes();
	        	}
	        }
    	  }
      }
      log.warn("numberOfTrips: " + numberOfTrips);
      log.warn("mean numberOfTrips: " + (int)numberOfTrips/2);
      Collections.sort(durations);
      int middle = ((durations.size()) / 2);
      if(durations.size() % 2 == 0){
		   double medianA = durations.get(middle);
		   double medianB = durations.get(middle+1);
		   medianValues.put("medianTotalDuration", (medianA + medianB) / 2);
      } else{
    	  medianValues.put("medianTotalDuration", durations.get(middle+1));       
      }
      meanValues.put("meanTotalDuration", sumValues.get("sumTotalDuration")/numberOfTrips); 
      
  }
  
  private void updateTotalEmissionStats(ArrayList<JsonResponseRoute> routeResults, String city, 
		  UserVehicle userVehicle){      
      for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
	        double totalEmissions = getTripTotalEmissions(trip, city, userVehicle);
	        if (maxValues.containsKey("maxTotalEmissions")){
	            double currentMaxTotalEmissions = (Double) maxValues.get("maxTotalEmissions");
	            if (currentMaxTotalEmissions < totalEmissions){
	                maxValues.put("maxTotalEmissions", totalEmissions);
	            }
	        }
	        else{
	            maxValues.put("maxTotalEmissions", totalEmissions);
	        }
	
	        if (minValues.containsKey("minTotalEmissions")){
	          double currentMinTotalEmissions = (Double) minValues.get("minTotalEmissions");
	          if (currentMinTotalEmissions > totalEmissions){
	              minValues.put("minTotalEmissions", totalEmissions);
	          }
	        }
	        else{
	            minValues.put("minTotalEmissions", totalEmissions);
	        }
	
	        if (sumValues.containsKey("sumTotalEmissions")){
	          double sumTotalEmissions = (Double) sumValues.get("sumTotalEmissions");
	          sumTotalEmissions += totalEmissions;
	          sumValues.put("sumTotalEmissions", sumTotalEmissions);
	        }
	        else{
	            sumValues.put("sumTotalEmissions", totalEmissions);
	        }
    	  }
      }
  }
  
  //per trip total duration
  private double getTripTotalDuration(JsonTrip trip){
//      double result = 0.0;
//      List<JsonSegment> segments = trip.getSegments();
//      int j = 0;
//      while (j < segments.size()) {
//    	  JsonSegment segment = segments.get(j);
//          result += (double)segment.getDurationMinutes();
//          j++;
//      }
//      return result;
	  return getTripTotalDuration2(trip);
  }
  
  //per trip total duration
  private double getTripTotalDuration2(JsonTrip trip){
      double result = 0.0;
      result = (double)trip.getDurationMinutes();
      return result;
  }
  
  //per trip total emissions
  private double getTripTotalEmissions(JsonTrip trip, String city, UserVehicle userVehicle){
      double result = 0.0;
      HashMap emissions = new HashMap<String, Double>();
      
      try{
	      if (userVehicle == null){
	    	  log.warn("user does not have any vehicles");
	    	  emissions.put("car", 198.0);
	      }
	      else{
	    	  emissions.put("car", 198.0);
	    	  if (userVehicle.getFeul_type().matches("Petrol")){
		    	  if (userVehicle.getVehicle_weight() < 2500){
		    		  if (userVehicle.getEngine_size()<1400){
		    			  emissions.put("car", 98.0);
		    		  }
		    		  else if (userVehicle.getEngine_size()>1400 && userVehicle.getEngine_size()<2000){
		    			  emissions.put("car", 109.0);
		    		  }
		    		  else if (userVehicle.getEngine_size()>2000){
		    			  emissions.put("car", 154.0);
		    		  }
		    	  }
		    	  else{
		    		  emissions.put("car", 241.0);
		    	  }
	    	  }
	    	  else if (userVehicle.getFeul_type().matches("Diesel")){
	    		  if (userVehicle.getVehicle_weight() < 2500){
		    		  if (userVehicle.getEngine_size()<1400){
		    			  emissions.put("car", 72.0);
		    		  }
		    		  else if (userVehicle.getEngine_size()>1400 && userVehicle.getEngine_size()<2000){
		    			  emissions.put("car", 89.0);
		    		  }
		    		  else if (userVehicle.getEngine_size()>2000){
		    			  emissions.put("car", 134.0);
		    		  }
		    	  }
		    	  else{
		    		  emissions.put("car", 253.0);
		    	  }
	    	  }
	      }
      }
      catch (Exception e){
    	  emissions.put("car", 169.0);
    	  e.printStackTrace();
      }
      
      if (city.matches("vienna")){
    	  emissions.put("ptMetro", 14.0);
          emissions.put("ptTrainS", 21.0);
          emissions.put("ptTrainR", 21.0);
          emissions.put("ptTrain", 21.0);
          emissions.put("ptTrainAirport", 21.0);
          emissions.put("ptTrainCog", 21.0);
          emissions.put("ptCableCar", 21.0);
          
          emissions.put("ptBusCity", 34.0);
          emissions.put("ptBusNight", 60.0);
          emissions.put("ptBusRegion", 34.0);
          
          emissions.put("ptTram", 21.0);
          emissions.put("ptTaxi", 198.0);
          
          emissions.put("walk", 0.0);
          emissions.put("bike", 0.0);
    	  
      }
      else if (city.matches("dublin")){
    	  emissions.put("ptMetro", 29.0);
          emissions.put("ptTrainS", 128.0);
          emissions.put("ptTrainR", 128.0);
          emissions.put("ptTrain", 66.45);
          emissions.put("ptTrainAirport", 128.0);
          emissions.put("ptTrainCog", 128.0);
          emissions.put("ptCableCar", 128.0);
          
          emissions.put("ptBusCity", 34.0);
          emissions.put("ptBusNight", 34.0);
          emissions.put("ptBusRegion", 34.0);
          
          emissions.put("ptTram", 128.0);
          emissions.put("ptTaxi", 198.0);
          
          emissions.put("walk", 0.0);
          emissions.put("bike", 0.0);
      }
      else{
    	  emissions.put("ptMetro", 20.0);
          emissions.put("ptTrainS", 50.0);
          emissions.put("ptTrainR", 55.5);
          emissions.put("ptTrain", 55.5);
          emissions.put("ptTrainAirport", 20.0);
          emissions.put("ptTrainCog", 20.0);
          emissions.put("ptCableCar", 20.0);
          
          emissions.put("ptBusCity", 25.5);
          emissions.put("ptBusNight", 25.5);
          emissions.put("ptBusRegion", 25.5);
          
          emissions.put("ptTram", 96.6);
          emissions.put("ptTaxi", 169.0);
          emissions.put("car", 169.0);
          emissions.put("walk", 0.0);
          emissions.put("bike", 0.0);
      }
      
      
      
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
    	  try{
    		  if (!(segment.getVehicle() == null) && !(segment.getVehicle().getType() == null)
    			  && !(segment.getDistanceMeter() == null)){
    		  
    			  result += ((Double)emissions.get(segment.getVehicle().getType()))
	    			  *(segment.getDistanceMeter()/1000.0);
    			  segment.addAttribute(AttributeListKeys.KEY_SEGMENT_CO2, Double.toString(((Double)emissions.get(segment.getVehicle().getType()))
    	    			  *(segment.getDistanceMeter()/1000.0)));
    		  }
    	  }
		  catch (Exception e){
			  log.warn("Could not get emmissions for vehicle type: " + segment.getVehicle().getType());
		  }    	  
          j++;
      }
      return result;
  }
  
  //per trip total emissions
  private double getTripNominalEmissions(JsonTrip trip){
      double result = 0.0;
      
      double nominalEmissions = 25.5; // this is the bus emissions
      
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
    	  if (!(segment.getVehicle() == null) && !(segment.getVehicle().getType() == null)){
	    	  result += nominalEmissions
	    			  *(segment.getDistanceMeter()/1000.0);
    	  }
          j++;
      }
      return result;
  }
  
//  private HashMap<String, Double> updateStatistics(ArrayList<JsonResponseRoute> routeResults){
//	  HashMap<String, Double> result = new HashMap<String, Double>();
//	  double minDuration = 10000;
//	  double maxDuration = 0;
//	  double medianDuration = 0;
//	  double meanDuration = 0;
//	  double minWalkingDuration = 10000;
//	  double maxWalkingDuration = 0;
//	  double minWalkingDurationPt = 10000;
//	  double maxWalkingDurationPt = 0;
//	  
//	  ArrayList<Double> durations = new ArrayList<Double>(); 
//	  
//	  for(JsonResponseRoute route : routeResults){
//    	  for(JsonTrip trip : route.getTrips()){
//	        double tripDuration = getTripTotalDuration(trip);
//	        durations.add(tripDuration);
//	        if (minDuration > tripDuration) minDuration = tripDuration;
//	        if (maxDuration < tripDuration) maxDuration = tripDuration;
//    	  }
//      }
//	  
//	  return null;
//  }
  
  //per trip total walking or bicycle duration
  private double getTripTotalWBDuration(JsonTrip trip){
	  double result = 0.0;      
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;      
      log.warn("getTripTotalWBDuration, segments.size(): " + segments.size());
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
          //if (trip.getModality().matches("walk") || trip.getModality().matches("bike")){
    	  if (segment.getType().matches("walk") || segment.getType().matches("bike")){
    		  result += (double)segment.getDurationMinutes();
        	  log.warn("segment " + j + " duration: " + (double)segment.getDurationMinutes());
          }
          j++;
      }
      log.warn("total duration with segments: " + result + " total duration trip: " + result);
      return result;
  }
  
  private LinkedHashMap<Integer, Double> calculateUserPreferences( long userId){
	  double[] data = {0, 0, 0, 0};
	  
	  LinkedHashMap<String, int[]> intervals = new LinkedHashMap();
	  intervals.put("morning", new int[]{1, 6});
	  intervals.put("noon", new int[]{6, 12});
	  intervals.put("afternoon", new int[]{12, 18});
	  intervals.put("evening", new int[]{18, 24});
	  
	  Calendar now = Calendar.getInstance();
	  SimpleDateFormat format = new SimpleDateFormat("H");
	  
	  int currentHour = Integer.parseInt(format.format(now.getTime()));
	  
	  //List<Stages> stages = stagesService.findStagesByUserId(userId);
	  // new solution that considers the hour of the day
	  List<Stages> stages = null;
	  if (currentHour >0 && currentHour <=6){
		  stages = stagesService.findStagesByUserIdAndHour(userId, 1, 6);
		  log.warn("morning, found: " + stages.size());
	  }
	  else if (currentHour >6 && currentHour <=12){
		  stages = stagesService.findStagesByUserIdAndHour(userId, 6, 12);
		  log.warn("noon, found: " + stages.size());
	  }
	  else if (currentHour >12 && currentHour <=18){
		  stages = stagesService.findStagesByUserIdAndHour(userId, 12, 18);
		  log.warn("afternoon, found: " + stages.size());
	  }
	  else if (currentHour >18 && currentHour <=24){
		  stages = stagesService.findStagesByUserIdAndHour(userId, 18, 24);
		  log.warn("evening, found: " + stages.size());
	  }
	  
	  LinkedHashMap<Integer, Double> result = new LinkedHashMap();
	  //0 walk, 1 bike, 2 pt, 3 car
	  
	  if (stages == null || stages.size() < 5){
		  data[0] = 4;
		  data[1] = 3;
		  data[2] = 2;
		  data[3] = 1;
	  }
	  if (stages != null){
		  for(Stages stage : stages){
			  switch (stage.getMode_detected_code()){
			  	case 1:
			  		data[0]++;
			  		break;
			  	case 2:
			  		data[1]++;
			  		break;
			  	case 3:
			  		data[3]++;
			  		break;
			  	case 4:
			  		data[3]++;
			  		break;
			  	case 5:
			  		data[2]++;
			  		break;
			  	case 6:
			  		data[2]++;
			  		break;
			  	case 7:
			  		data[2]++;
			  		break;
			  	case 11:
			  		data[2]++;
			  		break;
			  	}
			  }
	  }
	  result.put(4, data[0]); //walk
	  result.put(3, data[1]); //bicycle
	  result.put(2, data[2]); //pt
	  result.put(1, data[3]); //car
	  
	  List<Map.Entry<Integer, Double>> entries =
		  new ArrayList<Map.Entry<Integer, Double>>(result.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Integer, Double>>() {
		  public int compare(Map.Entry<Integer, Double> a, Map.Entry<Integer, Double> b){	
			  if (a.getValue().compareTo(b.getValue()) > 0){
				  return -1;
			  }
			  else if (a.getValue().compareTo(b.getValue()) < 0){
				  return 1;
			  }
			  else{
				  if (a.getKey() > b.getKey()){
					  return -1;
				  }else{
					  return 1;
				  }
					  
			  }		    
		  }
		});
		LinkedHashMap<Integer, Double> sortedResult = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Double> entry : entries) {
			sortedResult.put(entry.getKey(), entry.getValue());
		}
	    
		log.warn("order of routes display is the following:");
	    for (Map.Entry<Integer, Double> entry : sortedResult.entrySet()){
	    	log.warn(" type: " + entry.getKey() + " value: " + entry.getValue());
	    }
	  
	  return sortedResult;
  }
  
  private LinkedHashMap<Integer, Double> calculateUserHabits( long userId){
	  double[] data = {0, 0, 0, 0};
	  
	  LinkedHashMap<String, int[]> intervals = new LinkedHashMap();
	  intervals.put("morning", new int[]{1, 6});
	  intervals.put("noon", new int[]{6, 12});
	  intervals.put("afternoon", new int[]{12, 18});
	  intervals.put("evening", new int[]{18, 24});
	  
	  //List<Stages> stages = stagesService.findStagesByUserId(userId);
	  // new solution that considers the hour of the day
	  List<Stages> stages = null;
	  //fetching all stages
	  stages = stagesService.findStagesByUserIdAndHour(userId, 1, 24);
	  log.warn("morning, found: " + stages.size());	  
	  LinkedHashMap<Integer, Double> result = new LinkedHashMap();
	  //0 walk, 1 bike, 2 pt, 3 car
	  
	  if (stages == null || stages.size() < 5){
		  data[0] = 4;
		  data[1] = 3;
		  data[2] = 2;
		  data[3] = 1;
	  }
	  if (stages != null){
		  for(Stages stage : stages){
			  switch (stage.getMode_detected_code()){
			  	case 1:
			  		data[0]++;
			  		break;
			  	case 2:
			  		data[1]++;
			  		break;
			  	case 3:
			  		data[3]++;
			  		break;
			  	case 4:
			  		data[3]++;
			  		break;
			  	case 5:
			  		data[2]++;
			  		break;
			  	case 6:
			  		data[2]++;
			  		break;
			  	case 7:
			  		data[2]++;
			  		break;
			  	case 11:
			  		data[2]++;
			  		break;
			  	}
			  }
	  }
	  result.put(4, data[0]); //walk
	  result.put(3, data[1]); //bicycle
	  result.put(2, data[2]); //pt
	  result.put(1, data[3]); //car
	  
	  List<Map.Entry<Integer, Double>> entries =
		  new ArrayList<Map.Entry<Integer, Double>>(result.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Integer, Double>>() {
		  public int compare(Map.Entry<Integer, Double> a, Map.Entry<Integer, Double> b){	
			  if (a.getValue().compareTo(b.getValue()) > 0){
				  return -1;
			  }
			  else if (a.getValue().compareTo(b.getValue()) < 0){
				  return 1;
			  }
			  else{
				  if (a.getKey() > b.getKey()){
					  return -1;
				  }else{
					  return 1;
				  }
					  
			  }		    
		  }
		});
		LinkedHashMap<Integer, Double> sortedResult = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Double> entry : entries) {
			sortedResult.put(entry.getKey(), entry.getValue());
		}
	    
		log.warn("current user habits are the following:");
	    for (Map.Entry<Integer, Double> entry : sortedResult.entrySet()){
	    	log.warn(" type: " + entry.getKey() + " value: " + entry.getValue());
	    }	  
	  return sortedResult;
  }
  
  private int getNbrOfChanges(JsonTrip trip){
	  List<JsonSegment> segments = trip.getSegments();
		int k = 0;
		int result = 0;
		while (k < segments.size()) {
			JsonSegment segment = segments.get(k);
			if (!segment.getType().matches("walk") &&
					!segment.getType().matches("bike") &&
					!segment.getType().matches("change")){
				result++;
			}
			k++;
		}
	  return result;
  }
  
  	public int getWalkingTimeThreshold() {
		return walkingTimeThreshold;
	}
	
	public void setWalkingTimeThreshold(int walkingTimeThreshold) {
		this.walkingTimeThreshold = walkingTimeThreshold;
	}
  
	public int getBikeTimeThreshold() {
		return bikeTimeThreshold;
	}

	public void setBikeTimeThreshold(int bikeTimeThreshold) {
		this.bikeTimeThreshold = bikeTimeThreshold;
	}

	public int getMaxPTroutesToShow() {
		return maxPTroutesToShow;
	}

	public void setMaxPTroutesToShow(int maxPTroutesToShow) {
		this.maxPTroutesToShow = maxPTroutesToShow;
	}

	public double getTimeThresholdCutoff() {
		return timeThresholdCutoff;
	}

	public void setTimeThresholdCutoff(double timeThresholdCutoff) {
		this.timeThresholdCutoff = timeThresholdCutoff;
	}
	
	public String getMessageForPT() {
		return messageForPT;
	}

	public void setMessageForPT(String messageForPT) {
		this.messageForPT = messageForPT;
	}
	
	public String getMessageForWalk() {
		return messageForWalk;
	}

	public void setMessageForWalk(String messageForWalk) {
		this.messageForWalk = messageForWalk;
	}
	
	public double getThresholdForParkAndRide() {
		return thresholdForParkAndRide;
	}

	public void setThresholdForParkAndRide(double thresholdForParkAndRide) {
		this.thresholdForParkAndRide = thresholdForParkAndRide;
	}
	
	public double getWalkBikeThresholdForOmmitingCarParRoutes() {
		return walkBikeThresholdForOmmitingCarParRoutes;
	}

	public void setWalkBikeThresholdForOmmitingCarParRoutes(double walkBikeThresholdForOmmitingCarParRoutes) {
		this.walkBikeThresholdForOmmitingCarParRoutes = walkBikeThresholdForOmmitingCarParRoutes;
	}
	
	public double getPtChangesFactorThreshold() {
		return ptChangesFactorThreshold;
	}

	public void setPtChangesFactorThreshold(double ptChangesFactorThreshold) {
		this.ptChangesFactorThreshold = ptChangesFactorThreshold;
	}		
	
	public int getMaxBikeTimeInExtremeConditions() {
		return maxBikeTimeInExtremeConditions;
	}

	public void setMaxBikeTimeInExtremeConditions(int maxBikeTimeInExtremeConditions) {
		this.maxBikeTimeInExtremeConditions = maxBikeTimeInExtremeConditions;
	}

	public int getMaxWalkTimeInExtremeConditions() {
		return maxWalkTimeInExtremeConditions;
	}

	public void setMaxWalkTimeInExtremeConditions(int maxWalkTimeInExtremeConditions) {
		this.maxWalkTimeInExtremeConditions = maxWalkTimeInExtremeConditions;
	}
	
	public Integer getWalkingTimeThresholdForCarUsers() {
		return walkingTimeThresholdForCarUsers;
	}

	public void setWalkingTimeThresholdForCarUsers(
			Integer walkingTimeThresholdForCarUsers) {
		this.walkingTimeThresholdForCarUsers = walkingTimeThresholdForCarUsers;
	}

	public Integer getWalkingTimeThresholdForPTUsers() {
		return walkingTimeThresholdForPTUsers;
	}

	public void setWalkingTimeThresholdForPTUsers(
			Integer walkingTimeThresholdForPTUsers) {
		this.walkingTimeThresholdForPTUsers = walkingTimeThresholdForPTUsers;
	}

	public Integer getWalkingTimeThresholdForBikeUsers() {
		return walkingTimeThresholdForBikeUsers;
	}

	public void setWalkingTimeThresholdForBikeUsers(
			Integer walkingTimeThresholdForBikeUsers) {
		this.walkingTimeThresholdForBikeUsers = walkingTimeThresholdForBikeUsers;
	}

	public Integer getWalkingTimeThresholdForWalkUsers() {
		return walkingTimeThresholdForWalkUsers;
	}

	public void setWalkingTimeThresholdForWalkUsers(
			Integer walkingTimeThresholdForWalkUsers) {
		this.walkingTimeThresholdForWalkUsers = walkingTimeThresholdForWalkUsers;
	}

	public Integer getBikeTimeThresholdForCarUsers() {
		return bikeTimeThresholdForCarUsers;
	}

	public void setBikeTimeThresholdForCarUsers(Integer bikeTimeThresholdForCarUsers) {
		this.bikeTimeThresholdForCarUsers = bikeTimeThresholdForCarUsers;
	}

	public Integer getBikeTimeThresholdForPTUsers() {
		return bikeTimeThresholdForPTUsers;
	}

	public void setBikeTimeThresholdForPTUsers(Integer bikeTimeThresholdForPTUsers) {
		this.bikeTimeThresholdForPTUsers = bikeTimeThresholdForPTUsers;
	}

	public Integer getBikeTimeThresholdForBikeUsers() {
		return bikeTimeThresholdForBikeUsers;
	}

	public void setBikeTimeThresholdForBikeUsers(
			Integer bikeTimeThresholdForBikeUsers) {
		this.bikeTimeThresholdForBikeUsers = bikeTimeThresholdForBikeUsers;
	}

	public Integer getBikeTimeThresholdForWalkUsers() {
		return bikeTimeThresholdForWalkUsers;
	}

	public void setBikeTimeThresholdForWalkUsers(
			Integer bikeTimeThresholdForWalkUsers) {
		this.bikeTimeThresholdForWalkUsers = bikeTimeThresholdForWalkUsers;
	}
	
	public String getMessageForPT_De() {
		return messageForPT_De;
	}

	public void setMessageForPT_De(String messageForPT_De) {
		this.messageForPT_De = messageForPT_De;
	}

	public String getMessageForWalk_De() {
		return messageForWalk_De;
	}

	public void setMessageForWalk_De(String messageForWalk_De) {
		this.messageForWalk_De = messageForWalk_De;
	}

	public boolean checkForExtremeWeather(String city){
		try{
			double temp = 0.0;
			double precipitation = 0.0;
			boolean extremeConditions = false;
			boolean rainyConditions = false;
			boolean changeParams = false;
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR,-3);
			Date date = cal.getTime();	
			
			List<Citytemp> citytemps = citytempService.findCitytempByDate(date);
			
			if (citytemps.size() > 0){
				Citytemp citytemp = citytemps.get(0);
				temp = citytemp.getTemp();
				precipitation = citytemp.getPrecipitation();
				changeParams = true;
			}
			else{
				String url ="";
				if (city.matches("vienna")){
					url = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=Vienna&num_of_days=2&key=gxytvkzgssj753r6kb3aax68&format=csv";
				}
				else{
					url = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=Dublin&num_of_days=2&key=gxytvkzgssj753r6kb3aax68&format=csv";
				}
				URL flu = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) flu.openConnection();
		        conn.setRequestMethod( "GET" );
		        BufferedReader in = new BufferedReader(
		                                new InputStreamReader(
		                                conn.getInputStream()));
		        String inputLine;
		        String response = "";
		        int index = 0;
		        while ((inputLine = in.readLine()) != null){
		        	if (index == 8){
		        		response = inputLine;
		        	}
		        	index++;
		        }
		        in.close();
		        
		        String[] weatherValues = response.split(",");
		        
		        if (weatherValues.length > 2){
		        	temp = Double.parseDouble(weatherValues[1]);
		        	precipitation = Double.parseDouble(weatherValues[9]);
		        	changeParams = true;
		        	//save
		        	Citytemp newCitytemp = new Citytemp();
		        	newCitytemp.setCity(city);
		        	newCitytemp.setTemp(temp);
		        	newCitytemp.setPrecipitation(precipitation);
		        	newCitytemp.setTime((new Date()));
		        	citytempService.create(newCitytemp);
		        }
			}
			if (changeParams){
				if (temp < 5 || temp > 30){
	        		//extreme conditions
	        		extremeConditions = true;	       
	        	}
	        	if (precipitation > 1){
	        		//rain
	        		rainyConditions = true;
	        	}
	        	if (extremeConditions || rainyConditions){
	        		return true;
	        	}
			}
	    }
		catch(Exception e){
			log.warn("error in processing weather information");
			log.warn("check exception below");
			e.printStackTrace();
		}
		return false;
	}
	
static LinkedHashMap sortByValue(LinkedHashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
                  return ((Comparable) ((Map.Entry) (o2)).getValue())
                 .compareTo(((Map.Entry) (o1)).getValue());
             }
        });

       LinkedHashMap result = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
           Map.Entry entry = (Map.Entry)it.next();
           result.put(entry.getKey(), entry.getValue());
       }
       return result;
   }
  
}