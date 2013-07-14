package com.peacox.recommender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import com.fluidtime.brivel.route.json.RouteParser;
import com.fluidtime.library.model.json.request.RequestDetails;
import com.fluidtime.library.model.json.request.RequestGetRoute;
import com.fluidtime.library.model.json.request.RequestOptionRoute;
import com.peacox.recommender.repository.Citytemp;
import com.peacox.recommender.repository.CitytempService;
import com.peacox.recommender.repository.OwnedVehicles;
import com.peacox.recommender.repository.OwnedVehiclesService;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserRouteRequest;
import com.peacox.recommender.repository.UserRouteRequestService;
import com.peacox.recommender.repository.UserService;
import com.peacox.recommender.webservice.Webservice;

public class GetRecommendationForRequest {
	
	protected Logger log = Logger.getLogger(GetRecommendationForRequest.class);
	
	@Autowired protected OwnedVehiclesService ownedVehiclesService;
	
	@Autowired protected UserService userService;
	
	@Autowired protected UserRouteRequestService routeRequestService;
	
	@Autowired protected CitytempService citytempService;
	
	//arbitrary - is there a better solution?
	private int maxBikeTimeInExtremeConditions = 15;
	private int maxWalkTimeInExtremeConditions = 15;
	private int alwaysIncludeWalk = 1;
	private int alwaysIncludePT = 1;
	
	public String getRecommendation(String request){
		
		RequestGetRoute routeRequest = RouteParser
	                .routeRequestFromJson(request);
		
		log.debug("start processing RecommendationForRequest");
		log.debug("SessionId: " +routeRequest.getSessionId());
		log.debug("loaded property maxBikeTimeInExtremeConditions: " + this.getMaxBikeTimeInExtremeConditions());
		log.debug("loaded property maxWalkTimeInExtremeConditions: " + this.getMaxWalkTimeInExtremeConditions());
		log.debug("loaded property alwaysIncludeWalk: " + this.getAlwaysIncludeWalk());
		log.debug("loaded property alwaysIncludePT: " + this.getAlwaysIncludePT());
		
		Long userId = 46L;
		try{
			userId = Long.parseLong(routeRequest.getDetails().getClientId());
		}
		catch (Exception e){
			log.error("Could not load ClientId");
		}
		
		try{
			UserRouteRequest newRouteRequest = new UserRouteRequest();
			newRouteRequest.setUser_id(userId);
			newRouteRequest.setTimestamp(new Date());
			newRouteRequest.setRequest(request);
			newRouteRequest.setSessionId(routeRequest.getSessionId());
			routeRequestService.create(newRouteRequest);
		}catch(Exception e){
			log.error("Could not store routeRequest in the database");
		}
		
		//log.debug("route from: " + routeRequest.getFromName());
		//routeRequest.setModality(modality)
		//RequestDetails requestDetails = routeRequest.getDetails();
		
		HashSet<String> requestedModalities = routeRequest.getModality();
				
		routeRequest.setModality(fillModalities(requestedModalities, userId));
		
		RequestOptionRoute requestOptions = routeRequest.getOptionsRoute();
		requestOptions.setPtMobilityConstraints(fillDisabilities(requestOptions.getPtMobilityConstraints(), userId));
		
		requestOptions = setContext(requestOptions);
		
		String json = RouteParser.routeRequestToJson(routeRequest);
		
		log.debug("finished processing RecommendationForRequest. The result is: " + json);		
		 
		return json;
	}
	
	private HashSet<String> fillModalities(HashSet<String> modalities, Long userId){
		String[] availableModalities = {"pt", "car", "bike", "walk", "par",
				"bar", "bta"}; 
		
		//personalization
		//always add pt and walk as an available options
		if (modalities == null){
			modalities = new HashSet<String>();
		}
		
		if (!modalities.contains(availableModalities[0]) && this.getAlwaysIncludePT() == 1){
			modalities.add(availableModalities[0]);
		}
		if (!modalities.contains(availableModalities[3]) && this.getAlwaysIncludeWalk() == 1){
			modalities.add(availableModalities[3]);
		}
		
		//personalize according to owned vehicles 
		
		try{
			//fetch user vehicles
			// this is for bike and par only
			// if the user owns a bicycle include the bicycle [2]
			// if the user has requested a car - include the par [4] 
			//List<OwnedVehicles> ownedVehicles = ownedVehiclesService.findOwnedVehiclesByUserId(userId);
			
			//int j = 0;
			//log.debug("vehicles the user owns: " + ownedVehicles.size());
//			while (j < ownedVehicles.size()) {
//				log.debug("found vehicle:" + ownedVehicles.get(j).getType() + " for user " + userId);
//				if(ownedVehicles.get(j).getType().matches(availableModalities[2])){ // bike
//					if (!modalities.contains(availableModalities[2])){
//						modalities.add(availableModalities[2]);
//					}
//					if (!modalities.contains(availableModalities[5])){
//						modalities.add(availableModalities[5]);
//					}
//					if (!modalities.contains(availableModalities[6])){
//						modalities.add(availableModalities[6]);
//					}				
//				}
//				if(ownedVehicles.get(j).getType().matches(availableModalities[1])){ //car
//					if (modalities.contains(availableModalities[1])){ // add the park and ride option if the user owns a car						
//						modalities.add(availableModalities[4]);
//					}					
//				}
//				j++;
//			}
			
			if (modalities.contains(availableModalities[1])){ // add the park and ride option if the user owns a car						
				modalities.add(availableModalities[4]);
			}					
			
		}catch (Exception e){
			log.debug("could not load vehicles for user id: " + userId);
			log.debug("check exception below:");
			e.printStackTrace();
		}
		return modalities;
	}
	
	//personalize according to disabilities
	private HashSet<String> fillDisabilities(HashSet<String> mobilityConstraints, Long userId){
		
		String[] availableConstraints = {"ptNoStairs", "ptNoEscalators", "ptNoEvelators", "ptUseWheelchair"};
		
		try{
			User user = userService.findUserByUserId(userId);
			if (user == null){
				throw new Exception("Could not find user in the database");
			}
			log.debug("disabilities for user: " + user.getFirst_name() + " " + user.getLast_name());
			boolean hasDisabilities = user.isHas_disabilities();
			if (hasDisabilities){
				if (mobilityConstraints == null){
					mobilityConstraints = new HashSet<String>();
				}
				if(!mobilityConstraints.contains(availableConstraints[0]))
					mobilityConstraints.add(availableConstraints[0]);
				if(!mobilityConstraints.contains(availableConstraints[1]))
					mobilityConstraints.add(availableConstraints[1]);
			}			
		}
		catch (Exception e){
			log.debug("could not load disabilities for user id: " + userId);
			log.debug("check exception below:");
			e.printStackTrace();
		}
		return mobilityConstraints;		
	}
	
	//context (weather)
	private RequestOptionRoute setContext(RequestOptionRoute requestOptions){
		//contextType:

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
				String url = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=Vienna&num_of_days=2&key=gxytvkzgssj753r6kb3aax68&format=csv";
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
		        	newCitytemp.setCity("Vienna");
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
	        		requestOptions.setBikeMaxTime(this.getMaxBikeTimeInExtremeConditions());
	        		requestOptions.setWalkMaxTime(this.getMaxWalkTimeInExtremeConditions());
	        	}
			}
	    }
		catch(Exception e){
			log.debug("error in processing weather information");
			log.debug("check exception below");
			e.printStackTrace();
		}
		return requestOptions;
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

	public int getAlwaysIncludeWalk() {
		return alwaysIncludeWalk;
	}

	public void setAlwaysIncludeWalk(int alwaysIncludeWalk) {
		this.alwaysIncludeWalk = alwaysIncludeWalk;
	}

	public int getAlwaysIncludePT() {
		return alwaysIncludePT;
	}

	public void setAlwaysIncludePT(int alwaysIncludePT) {
		this.alwaysIncludePT = alwaysIncludePT;
	}

}
