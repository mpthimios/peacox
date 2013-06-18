package com.peacox.recommender.webservice;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.net.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fluidtime.brivel.route.json.AttributeListKeys;
import com.fluidtime.brivel.route.json.RouteParser;
import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.peacox.recommender.GetRecommendationForONXRequest;
import com.peacox.recommender.GetRecommendationForRequest;
import com.peacox.recommender.GetRecommendations;
import com.peacox.recommender.GetRecommendationsRouteDto;
import com.peacox.recommender.RouteRequest;
import com.peacox.recommender.UserPreferences;
import com.peacox.recommender.repository.EmissionStatistics;
import com.peacox.recommender.repository.EmissionStatisticsService;
import com.peacox.recommender.repository.OwnedVehicles;
import com.peacox.recommender.repository.Recommendations;
import com.peacox.recommender.repository.RecommendationsService;
import com.peacox.recommender.repository.Stages;
import com.peacox.recommender.repository.StagesService;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserRouteRequest;
import com.peacox.recommender.repository.UserRouteRequestService;
import com.peacox.recommender.repository.UserRouteResult;
import com.peacox.recommender.repository.UserRouteResultService;
import com.peacox.recommender.repository.UserService;
//import com.peacox.recommender.repository.OwnedVehicles;
//import com.peacox.recommender.repository.OwnedVehiclesTypeService;
import com.peacox.recommender.repository.OwnedVehiclesService;
import com.peacox.recommender.utils.AverageEmissions;
import com.peacox.recommender.utils.CompressString;
import com.peacox.recommender.utils.Coordinates;
import com.peacox.recommender.utils.Simulator;
import com.peacox.recommender.utils.TreeScore;

@Controller
@RequestMapping(value = "/")
public class Webservice {

	@Autowired	
	protected OwnedVehiclesService ownedVehiclesService;

	@Autowired	
	protected UserService userService;
	
	@Autowired
	private ApplicationContext appContext;
	
	@Autowired
	private UserRouteResultService routeResultService;
	
	@Autowired
	private RecommendationsService recommendationsService;
	
	@Autowired
	private StagesService stagesService;
	
	@Autowired
	private EmissionStatisticsService emissionStatisticsService;
	
	protected String MODE="TESTING"; // "SIMULATION" "PRODUCTION" "DIPLOMA"
	
	protected String REQUEST_MODE="DIPLOMA"; //"PRODUCTION"
	
	protected String[] weatherTemp = {"cold", "mild", "hot"};
	protected String[] weatherCond = {"rainy", "cloudy", "sunny"};
	
	protected Logger log = Logger.getLogger(Webservice.class);
	
	protected String bicycle = "bicycle";
	protected String car = "car";
	
	@RequestMapping(method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		try {
			//testJPA();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.LONG, locale);
		String formattedDate = dateFormat.format(date);
		model.addAttribute("serverResponse", formattedDate);
		User user = userService.findUserByUserId(3L);
		log.debug("calculating for user: " + user.getFirst_name() + " " + user.getLast_name());	
		//List<OwnedVehicles> ownedVehicles = ownedVehiclesService.findOwnedVehiclesByUserId(3L);
		return "welcome";
	}
	
	@RequestMapping(value="getRecommendationForRequest", method = RequestMethod.POST)
	public String getRecommendationForRequest (Locale locale, Model model, @RequestBody String body) {
		
		log.debug("getRecommendationForRequest");
		log.debug("received new RecommendationForRequest: " + body);
		
		try {
			//testJPA();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (REQUEST_MODE.matches("DIPLOMA")){
			GetRecommendationForONXRequest requestRecommendation = 
					(GetRecommendationForONXRequest) appContext.getBean("GetRecommendationForONXRequest");
			model.addAttribute("serverResponse", requestRecommendation.getRecommendation(body));
			
			return "getRecommendationForRequest";
		}
		
		GetRecommendationForRequest requestRecommendation = 
				(GetRecommendationForRequest) appContext.getBean("GetRecommendationForRequest");
		//GetRecommendationForRequest requestRecommendation = new GetRecommendationForRequest();
		model.addAttribute("serverResponse", requestRecommendation.getRecommendation(body));
		
		return "getRecommendationForRequest";
	}
	
	@RequestMapping(value="getRecommendationForRoute", method = RequestMethod.POST)
	public String getRecommendationForRoute(Locale locale, Model model, @RequestBody String body) {
		
		log.debug("getRecommendationForRoute");
		log.debug("received new RecommendationForRequest: " + body);
		
		JsonResponseRoute route = RouteParser.routeFromJson(body);
		
		String userIdStr = route.getAttribute(AttributeListKeys.KEY_ROUTE_USERID);
		Long userId = 0L;
		
		if (userIdStr != null){
			userId = Long.parseLong(userIdStr);
		}
		else{
			// no userid - exit!
			log.error("UserId in the route request is null - exiting");
			model.addAttribute("serverResponse", body);			
			return "getRecommendationForRoute";
		}
		
		try{
			UserRouteResult newRouteResult = new UserRouteResult();
			newRouteResult.setUser_id(userId);
			newRouteResult.setTimestamp(new Date());
			newRouteResult.setResult(CompressString.compress(body));
			routeResultService.create(newRouteResult);
			//log.debug("testing compressed String: " + newRouteResult.getResult() + " sdfs");
			//log.debug("testing decompressed String: " + CompressString.decompress(newRouteResult.getResult()));
		}catch(Exception e){
			log.error("Could not store routeRequest in the database");
			e.printStackTrace();
		}
		
		UserPreferences userPreferences = new UserPreferences();
		int scenarioId = 1;
		
		User user = userService.findUserByUserId(userId);
		
		if (user == null){
			log.error("UserId in the database is null - exiting");
			model.addAttribute("serverResponse", body);			
			return "getRecommendationForRoute";
		}
		
		log.debug("calculating for user: " + user.getFirst_name() + " " + user.getLast_name());		
		String jsonResponse = recommendRoutes(route, userPreferences, userId);
        
        //log.debug("jsonResponse: " + jsonResponse);
        try{
			Recommendations recommendations = new Recommendations();
			recommendations.setUser_id(45);
			recommendations.setTimestamp(new Date());
			recommendations.setRecommendations(CompressString.compress(jsonResponse));
			recommendationsService.create(recommendations);
			
			//log.debug("testing compressed String: " + newRouteResult.getResult() + " sdfs");
			//log.debug("testing decompressed String: " + CompressString.decompress(newRouteResult.getResult()));
			
		}catch(Exception e){
			log.error("Could not store routeRequest in the database");
			e.printStackTrace();
		}
        
        model.addAttribute("serverResponse", jsonResponse);
		
		return "getRecommendationForRoute";
	}
	
	@RequestMapping(value="getTestRecommendationForRoute", method = RequestMethod.POST)
	public String getTestRecommendationForRoute(Locale locale, Model model, @RequestBody String body) {
		
		System.out.println(body);
		
		if(true){
			try{
				model.addAttribute("serverResponse", body);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return "getRecommendationForRoute";
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();//Gson();
		RouteRequest routeRequest = gson.fromJson(body, RouteRequest.class);				
		log.debug("user: " + routeRequest.getUserId());
		
		Long userId = Long.parseLong(routeRequest.getUserId());
		UserPreferences userPreferences = new UserPreferences();
		int scenarioId = 1;
		String modeOfTransport = "pt";
		
		User user = userService.findUserByUserId(userId);
		log.debug("calculating for user: " + user.getFirst_name() + " " + user.getLast_name());
//		List<OwnedVehicles> ownedVehicles = ownedVehiclesService.findOwnedVehiclesByUserId(userId);
//		
//		int j = 0;
//		log.debug("vehicles the user owns: " + ownedVehicles.size());
//		while (j < ownedVehicles.size()) {
//			log.debug(ownedVehicles.get(j).getType());
//			if(ownedVehicles.get(j).getType().matches(bicycle))
//				modeOfTransport = modeOfTransport+"|bike";
//			else if(ownedVehicles.get(j).getType().matches(car))
//				modeOfTransport = modeOfTransport+"|car";
//			j++;
//		}
		
		try{
			
			if (MODE.matches("SIMULATION")){
				
				
				//runSimulation();
			}
			else if (MODE.matches("TESTING")){
				String[] fromCoordinates = new String[2];
				String[] toCoordinates = new String[2];
				if (routeRequest.getCoordinatesType().matches("WGS84")){
					log.debug("address format: WGS84");
					fromCoordinates[0] = routeRequest.getFromWGS84Lat();
					fromCoordinates[1] = routeRequest.getFromWGS84Lon();
					toCoordinates[0] = routeRequest.getToWGS84Lat();
					toCoordinates[1] = routeRequest.getToWGS84Lon();
				}
				else{
					log.debug("address format: plain");
					fromCoordinates = Coordinates.GeocodeAddress(routeRequest.getFromStr());
			        toCoordinates = Coordinates.GeocodeAddress(routeRequest.getToStr());
				}								
				
		        String url = buildUrl(fromCoordinates, toCoordinates, modeOfTransport);
		        //"bar|car|par|pt|walk|bike|bta"
		        log.debug(url);
		        
		        URL flu = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) flu.openConnection();
		        conn.setRequestMethod( "POST" );
		        BufferedReader in = new BufferedReader(
		                                new InputStreamReader(
		                                conn.getInputStream()));
		        String inputLine;
		        String response = "";
		        while ((inputLine = in.readLine()) != null) 
		        	response += inputLine;
		        in.close();		        		        
		       
		        System.out.println(response);
		        JsonResponseRoute route = RouteParser.routeFromJson(response);
		        String jsonResponse = recommendRoutes(route, userPreferences);
		        
		        model.addAttribute("serverResponse", jsonResponse);
				
			}
			else {
				//PRODUCTION CODE - TOBE COMPLETED		        		   
			}	        
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		return "getRecommendationForRoute";
	}

	@RequestMapping(value="calculateEmissions", method = RequestMethod.GET)
	public String calculateEmissions (Locale locale, Model model, @RequestBody String body) {
		
		log.debug("calculateEmissions");
		log.debug("received new calculateEmissions: " + body);
		
		try {
			List<User> users = userService.findAllUsers();
			log.debug("number of users found: " + users.size());
			for (User user : users){
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY,0);
				cal.set(Calendar.MINUTE,0);
				cal.set(Calendar.SECOND,0);
				cal.set(Calendar.MILLISECOND,0);
				Date start = cal.getTime();				
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE,59);
				cal.set(Calendar.SECOND,59);
				cal.set(Calendar.MILLISECOND,999);
				Date end = cal.getTime();
				
				List<Stages> stages = stagesService.findStagesByUserIdAndDate(user.getId(), start, end);				
				log.debug("number of stages found: " + stages.size());
				
				for(Stages stage : stages){
					double dailyEmissions = 0.0;
					int mode = stage.getMode_detected_code();
					dailyEmissions += AverageEmissions.getLarasAverageEmissions(mode)
							*stage.getDistance()/1000.0;
					EmissionStatistics emissionStatistics = new EmissionStatistics();
					emissionStatistics.setStage_id(stage.getId());
					emissionStatistics.setTimestamp(new Timestamp((new Date()).getTime()));
					emissionStatistics.setEmissions_estimation(dailyEmissions);
					emissionStatisticsService.create(emissionStatistics);
				}
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		model.addAttribute("serverResponse", "DONE");
		
		return "calculateEmissions";
	}
	
	@RequestMapping(value="getStaticUserScoreForTree", method = RequestMethod.POST)
	public String getUserScoreForTree (Locale locale, Model model, @RequestParam long userId) {
		
		log.debug("getStaticUserScoreForTree");
		log.debug("received new getStaticUserScoreForTree with user id: " + userId);
		double result = 0;
		try{
			//long userId = Long.parseLong(body);
			TreeScore treeScore = 
					(TreeScore) appContext.getBean("TreeScore");
			result = treeScore.calculateStatic(userId);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		model.addAttribute("userScore", result);
		return "getUserScoreForTree";
	}
	
	@RequestMapping(value="getDynamicUserScoreForTree", method = RequestMethod.POST)
	public String getDynamicUserScoreForTree (Locale locale, Model model, @RequestParam long userId) {
		
		log.debug("getDynamicUserScoreForTree");
		log.debug("received new getDynamicUserScoreForTree with user id: " + userId);
		double result = 0;
		try{
			//long userId = Long.parseLong(body);
			TreeScore treeScore = 
					(TreeScore) appContext.getBean("TreeScore");
			result = treeScore.calculateDynamic(userId);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		model.addAttribute("userScore", result);
		return "getUserScoreForTree";
	}
	
	//without userId
	private String recommendRoutes(JsonResponseRoute route, UserPreferences userPreferences){
		
        //printRouteInfo(route);
        
        ArrayList routeList = new ArrayList<JsonResponseRoute>();
        routeList.add(route);
        
        GetRecommendations recommendations = 
				(GetRecommendations) appContext.getBean("GetRecommendations");
        LinkedHashMap<Integer, HashMap<JsonTrip,Double>> finalRouteResults = recommendations.getRecommendations(userPreferences, routeList);
        List<JsonTrip> newTrips = new ArrayList();
        
        //maybe temporary solution: empty route trips and add the in the order I want
        route.getTrips().clear(); // this is temporary
        for (Map.Entry<Integer, HashMap<JsonTrip,Double>> entry : finalRouteResults.entrySet()) {
            Integer key = entry.getKey();
            HashMap<JsonTrip,Double> value = entry.getValue();
            log.debug("***********Found Trip: " + key + " ***********");
            Map.Entry<JsonTrip,Double> element = value.entrySet().iterator().next();
            double utility = element.getValue();
            log.debug("entry utility: " + element.getValue());
            JsonTrip trip = element.getKey();
            log.debug("Trip Info:");
            trip.addAttribute(AttributeListKeys.KEY_TRIP_INDEX, Integer.toString(key));
            trip.addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_FACTOR, Double.toString(utility));
            printTripInfo(trip);
            newTrips.add(trip);
            //start this is temporary
            //int tripIndex = route.getTrips().indexOf(trip);
            //route.getTrips().get(tripIndex).addAttribute(AttributeListKeys.KEY_TRIP_INDEX, Integer.toString(key));                      
            //route.getTrips().get(tripIndex).addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_FACTOR, Double.toString(utility));
            //log.debug("adding trip_index: " + Integer.toString(key) + " to trip " + tripIndex + " with RECOMMENDATION_FACTOR: " + Double.toString(utility));
            //end this is temporary
            log.debug("*********** END Found Trip ***********");
        }
        route.setTrips(newTrips); 
        
        //route.setTrips(newTrips);
        
        String json = RouteParser.routeToJson(route);
        
        return json;
	}
	
	//with userId
	private String recommendRoutes(JsonResponseRoute route, UserPreferences userPreferences, long userId){
		
        //printRouteInfo(route);
        
        ArrayList routeList = new ArrayList<JsonResponseRoute>();
        routeList.add(route);
        		       
        GetRecommendations recommendations = 
				(GetRecommendations) appContext.getBean("GetRecommendations");
        LinkedHashMap<Integer, HashMap<JsonTrip,Double>> finalRouteResults = 
        		recommendations.getRecommendations(userPreferences, routeList, userId);
        List<JsonTrip> newTrips = new ArrayList();
        
        //maybe temporary solution: empty route trips and add the in them order I want
        route.getTrips().clear(); // this is temporary
        for (Map.Entry<Integer, HashMap<JsonTrip,Double>> entry : finalRouteResults.entrySet()) {
            Integer key = entry.getKey();
            HashMap<JsonTrip,Double> value = entry.getValue();
            log.debug("***********Found Trip: " + key + " ***********");
            Map.Entry<JsonTrip,Double> element = value.entrySet().iterator().next();
            double utility = element.getValue();
            log.debug("entry utility: " + element.getValue());
            JsonTrip trip = element.getKey();
            log.debug("Trip Info:");
            trip.addAttribute(AttributeListKeys.KEY_TRIP_INDEX, Integer.toString(key));
            trip.addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_FACTOR, Double.toString(utility));
            printTripInfo(trip);
            newTrips.add(trip);
            //start this is temporary
            //int tripIndex = route.getTrips().indexOf(trip);
            //route.getTrips().get(tripIndex).addAttribute(AttributeListKeys.KEY_TRIP_INDEX, Integer.toString(key));                      
            //route.getTrips().get(tripIndex).addAttribute(AttributeListKeys.KEY_TRIP_RECOMMENDATION_FACTOR, Double.toString(utility));
            //log.debug("adding trip_index: " + Integer.toString(key) + " to trip " + tripIndex + " with RECOMMENDATION_FACTOR: " + Double.toString(utility));
            //end this is temporary
            log.debug("*********** END Found Trip ***********");
        }
        route.setTrips(newTrips); 
        
        //route.setTrips(newTrips);
        
        String json = RouteParser.routeToJson(route);
        
        return json;
	}

	private void testJPA() throws Exception {
		
	}
	
	private void runSimulation() throws Exception {
		Simulator.RunSimulation();
	}
	
	private void printRouteInfo(JsonResponseRoute route) { // for debugging
		log.debug("routeid: " + route.getId());
		log.debug("trips: " + route.getTrips().size());
		List<JsonTrip> trips = route.getTrips();
		int j = 0;
		
		while (j < trips.size()) {
			log.debug("*******" + " New Trip Found " + "*******");
			JsonTrip trip = trips.get(j);
			printTripInfo(trip);
			j++;
		}
	}
	
	private void printTripInfo(JsonTrip trip) { // for debugging				
		log.debug("modality: " + trip.getModality() + 
				" - Total Distance: " + trip.getDistanceMeter() + " meters" +
				" - Total Duration " + trip.getDurationMinutes() + " minutes" +
				" - Total Emissions " + trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2) + " CO2");			
		List<JsonSegment> segments = trip.getSegments();
		int k = 0;
		while (k < segments.size()) {
			log.debug(" Segment: " + k + ":");
			JsonSegment segment = segments.get(k);
			printSegmentInfo(segment);
			k++;
		}
		log.debug("*******" + " END New Trip Found " + "*******");		
	}
	
	private void printSegmentInfo(JsonSegment segment) { // for debugging				
		try{						
			String from = "unknown";
			String to = "unknown";
			if (!(segment.getLocationFrom() == null) && 
					!(segment.getLocationFrom().getProperties() == null)
					&& !(segment.getLocationFrom().getProperties().getName() == null)) 
					from = segment.getLocationFrom().getProperties().getName();
			if (!(segment.getLocationTo() == null) && 
					!(segment.getLocationTo().getProperties() == null)
					&& !(segment.getLocationTo().getProperties().getName() == null)) 
					to = segment.getLocationTo().getProperties().getName();
			
			log.debug(" mode of transport: " + segment.getType() + " - " + 
					"From: " + from +
					" To: " + to +
					" - distance: " + segment.getDistanceMeter() + " meters " + 
					" - duration: " + segment.getDurationMinutes() + " minutes ");
			
		}catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	private String buildUrl (String[] from, String[] to, String modality){
		
		MessageFormat url = new MessageFormat("http://dev.webservice.peacox.fluidtime.com/ws/1.0/getRoute?from={0}:{1}:WGS84:AddressFrom&to={2}:{3}:WGS84:AddressTo&modality={4}");
		Object[] args = { from[0], from[1], to[0], to[1], modality};
		
		return url.format(args);
	}
}
