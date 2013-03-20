package com.peacox.recommender.webservice;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.net.*;
import java.io.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fluidtime.brivel.route.json.RouteParser;
import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.fluidtime.brivel.route.json.response.JsonResponseRouteTrip;
import com.fluidtime.brivel.route.json.response.JsonResponseSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.peacox.recommender.UserPreferences;
import com.peacox.recommender.repository.OwnedVehicles;
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

	protected String MODE="PRODUCTION"; // "SIMULATION"
	
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
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();//Gson();
		UserPreferences userPreferences = gson.fromJson(body, UserPreferences.class);
						
		String json = gson.toJson(userPreferences);
		
		List<OwnedVehicles> ownedVehicles = ownedVehiclesService.findOwnedVehiclesByUserId(1);
		
		int j = 0;
		while (j < ownedVehicles.size()) {
			System.out.println(ownedVehicles.get(j));
			j++;
		}
		
		//System.out.println(" owned vehicles: " + ownedVehicles.getType());
		
		try{
			
			if (MODE.matches("SIMULATION")){
				   runSimulation();
			}
			else{
				URL flu = new URL("http://dev.webservice.peacox.fluidtime.com/ws/1.0/getRoute?from=48.23748:16.38598:WGS84:AddressFrom&to=48.287035:16.419471:WGS84:AddressTo&modality=pt");
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
		        
		        //response = response.replaceAll("\\r|\\n", "");
		       
		        model.addAttribute("serverTime", response);
		        
		        System.out.println(response);
		        
		        JsonResponseRoute route = RouteParser.jsonStringTojsonRoute(response);
		        printRouteInfo(route);
		        
		        String[] coordinates = Coordinates.GeocodeAddress("Wien, Vorgartenstrasse 65");		        		    
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
			System.out.println("modality: " + trip.getModality());			
			List<JsonSegment> segments = trip.getSegments();
			int k = 0;
			while (k < segments.size()) {
				System.out.println("*******" + " New Segment Found " + "*******");
				JsonSegment segment = segments.get(k);
				System.out.println(" " + segment.getVehicle());
				try{
					//System.out.println(" segment.getPathPolygon().getProperties().toString(): " + segment.getPathPolygon().getProperties().toString());
					System.out.println(" segment.getPathPolygon().getProperties().toString(): " + segment);
					System.out.println(" distance: " + segment.getDistanceMeter());
					System.out.println(" duration: " + segment.getDurationMinutes());
					//System.out.println(" distance: " + segment.getPathPolygon().getProperties().toString());
				}catch (Exception e){
					e.printStackTrace();
				}
				System.out.println("*******" + " END New Segment Found " + "*******");
				k++;
			}
			System.out.println("*******" + " END New Trip Found " + "*******");
			j++;
		}
		//print some info for the route:
        //System.out.println("from: " + routeDto.getLocationFrom().getTitle());
        //System.out.println("to: " + routeDto.getLocationTo().getTitle());
//        List<TripDto> trips = routeDto.getTrips();      
//        for (Iterator it = trips.iterator(); it.hasNext();){
//            TripDto trip = (TripDto) it.next();
//            System.out.println("tripId: " +  trip.getId() 
//                    + " MoT: " + trip.getModality()
//                    + " duration: " + trip.getDurationMinutes()
//                    + " emissions: " + trip.getDescription());          
//        }
	}

}
