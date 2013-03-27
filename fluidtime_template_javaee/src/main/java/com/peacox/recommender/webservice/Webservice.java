package com.peacox.recommender.webservice;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fluidtime.brivel.route.json.RouteParser;
import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.library.model.json.FeatureTypes.JsonFeature;
import com.fluidtime.library.model.json.request.RequestGetRoute;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.fluidtime.brivel.route.json.response.JsonResponseRouteTrip;
import com.fluidtime.brivel.route.json.response.JsonResponseSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.peacox.recommender.GetRecommendations;
import com.peacox.recommender.GetRecommendationsRouteDto;
import com.peacox.recommender.UserPreferences;
import com.peacox.recommender.repository.OwnedVehicles;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserService;
//import com.peacox.recommender.repository.OwnedVehicles;
//import com.peacox.recommender.repository.OwnedVehiclesTypeService;
import com.peacox.recommender.repository.OwnedVehiclesService;
import com.peacox.recommender.utils.Coordinates;
import com.peacox.recommender.utils.Simulator;

@Controller
@RequestMapping(value = "/")
public class Webservice {

	@Autowired	
	protected OwnedVehiclesService ownedVehiclesService;

	@Autowired	
	protected UserService userService;
	
	protected String MODE="TESTING"; // "SIMULATION" "PRODUCTION"
	
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
		model.addAttribute("serverTime", formattedDate);
		return "welcome";
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String postHandler(Locale locale, Model model, @RequestBody String body) {
		
		System.out.println(body);
		
		//Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();//Gson();
		//UserPreferences userPreferences = gson.fromJson(body, UserPreferences.class);				
		//String json = gson.toJson(userPreferences);
		
		String fromAddress = "";
		String toAddress = "";
		Long userId = 1L;
		UserPreferences userPreferences = new UserPreferences();
		int scenarioId = 1;
		String modeOfTransport = "pt";
		
		User user = userService.findUserByUserId(userId);
		log.debug("calculating for user: " + user.getFirst_name() + " " + user.getLast_name());
		List<OwnedVehicles> ownedVehicles = ownedVehiclesService.findOwnedVehiclesByUserId(userId);
		
		int j = 0;
		log.debug("vehicles the user owns:");
		while (j < ownedVehicles.size()) {
			log.debug(ownedVehicles.get(j).getType());
			if(ownedVehicles.get(j).getType().matches(bicycle))
				modeOfTransport = modeOfTransport+"|bike";
			else if(ownedVehicles.get(j).getType().matches(car))
				modeOfTransport = modeOfTransport+"|car";
			j++;
		}
		
		try{
			
			if (MODE.matches("SIMULATION")){
				
				//runSimulation();
			}
			else if (MODE.matches("TESTING")){
				String[] fromCoordinates = Coordinates.GeocodeAddress("Wien, Webgasse 6");
		        String[] toCoordinates = Coordinates.GeocodeAddress("Wien, Universitatsring 65");
				
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
		        
		        JsonResponseRoute route = RouteParser.jsonStringTojsonRoute(response);
		        //printRouteInfo(route);
		        
		        ArrayList routeList = new ArrayList<JsonResponseRoute>();
		        routeList.add(route);
		        		       
		        GetRecommendations recommendations = new GetRecommendations();
		        LinkedHashMap<Integer, HashMap<JsonTrip,Double>> finalRouteResults = recommendations.getRecommendations(userPreferences.getUserPreferences(), routeList);
		        List<JsonTrip> newTrips = new ArrayList();
		        for (Map.Entry<Integer, HashMap<JsonTrip,Double>> entry : finalRouteResults.entrySet()) {
		            Integer key = entry.getKey();
		            HashMap<JsonTrip,Double> value = entry.getValue();
		            System.out.println("***********Found Trip: " + key + " ***********");
		            Map.Entry<JsonTrip,Double> element = value.entrySet().iterator().next();
		            System.out.println("entry utility: " + element.getValue());
		            JsonTrip trip = element.getKey();
		            System.out.println("Trip Info:");
		            printTripInfo(trip);
		            newTrips.add(trip);
		            System.out.println("*********** END Found Trip ***********");
		        }
		        
		        route.setTrips(newTrips);
		        
		        String json = RouteParser.routeToJson(route);
		        
		        model.addAttribute("serverResponse", json);
				
			}
			else {
				//PRODUCTION CODE - TOBE COMPLETED		        		   
			}	        
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		return "welcome";
	}

	private void testJPA() throws Exception {
		
	}
	
	private void runSimulation() throws Exception {
		Simulator.RunSimulation();
	}
	
	private void printRouteInfo(JsonResponseRoute route) { // for debugging
		System.out.println("routeid: " + route.getId());
		System.out.println("trips: " + route.getTrips().size());
		List<JsonTrip> trips = route.getTrips();
		int j = 0;
		
		while (j < trips.size()) {
			System.out.println("*******" + " New Trip Found " + "*******");
			JsonTrip trip = trips.get(j);
			printTripInfo(trip);
			j++;
		}
	}
	
	private void printTripInfo(JsonTrip trip) { // for debugging				
		System.out.println("modality: " + trip.getModality() + 
				" - Total Distance: " + trip.getDistanceMeter() + " meters" +
				" - Total Duration " + trip.getDurationMinutes() + " minutes");			
		List<JsonSegment> segments = trip.getSegments();
		int k = 0;
		while (k < segments.size()) {
			System.out.println(" Segment: " + k + ":");
			JsonSegment segment = segments.get(k);
			printSegmentInfo(segment);
			k++;
		}
		System.out.println("*******" + " END New Trip Found " + "*******");		
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
			
			System.out.println(" mode of transport: " + segment.getType() + " - " + 
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
