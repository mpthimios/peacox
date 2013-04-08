package com.peacox.recommender;

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
import com.peacox.recommender.repository.OwnedVehicles;
import com.peacox.recommender.repository.OwnedVehiclesService;
import com.peacox.recommender.webservice.Webservice;

@Component("GetRecommendationForRequest")
public class GetRecommendationForRequest {
	
	protected Logger log = Logger.getLogger(GetRecommendationForRequest.class);
	
	@Autowired protected OwnedVehiclesService ownedVehiclesService;
	
	public String getRecommendation(String request){
		
		RequestGetRoute routeRequest = RouteParser
	                .routeRequestFromJson(request);
		
		log.debug("route from: " + routeRequest.getFromName());
		//routeRequest.setModality(modality)
		RequestDetails requestDetails = routeRequest.getDetails();
		
		HashSet<String> modalities = routeRequest.getModality();
						
		routeRequest.setModality(fillModalities(modalities, 3L));
		RequestOptionRoute requestOptions = routeRequest.getOptionsRoute();
		
		
		String json = RouteParser.routeRequestToJson(routeRequest);
		log.debug("route to: " + json);		
		 
		return json;
	}
	
	HashSet<String> fillModalities(HashSet<String> modalities, Long userId){
		String[] availableModalities = {"pt", "car", "bike", "walk", "par",
				"bar", "bta"}; 
		
		//always add pt and walk as an available options
		if (!modalities.contains(availableModalities[0])){
			modalities.add(availableModalities[0]);
		}
		if (!modalities.contains(availableModalities[3])){
			modalities.add(availableModalities[3]);
		}
		
		List<OwnedVehicles> ownedVehicles = ownedVehiclesService.findOwnedVehiclesByUserId(userId);
		try{
			//fetch user vehicles
			
			
			int j = 0;
			log.debug("vehicles the user owns:");
			while (j < ownedVehicles.size()) {
				log.debug("found vehicle:" + ownedVehicles.get(j).getType() + " for user " + userId);
				if(ownedVehicles.get(j).getType().matches(availableModalities[2])){ // bike
					if (!modalities.contains(availableModalities[2])){
						modalities.add(availableModalities[2]);
					}
					if (!modalities.contains(availableModalities[5])){
						modalities.add(availableModalities[5]);
					}
					if (!modalities.contains(availableModalities[6])){
						modalities.add(availableModalities[6]);
					}				
				}
				if(ownedVehicles.get(j).getType().matches(availableModalities[1])){ //car
					if (!modalities.contains(availableModalities[1])){
						modalities.add(availableModalities[1]);
					}
					if (!modalities.contains(availableModalities[4])){
						modalities.add(availableModalities[4]);
					}					
				}
				j++;
			}
		}catch (Exception e){
			log.debug("could not load vehicles for user id: " + userId);
			log.debug("check exception below:");
			e.printStackTrace();
		}
		
		return modalities;
	}

}
