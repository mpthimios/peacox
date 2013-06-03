package com.peacox.recommender.utils;

import java.util.List;

import org.apache.log4j.Logger;

import com.fluidtime.brivel.route.json.AttributeListKeys;
import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.peacox.recommender.GetRecommendationForRequest;


public class Reports
{
	protected Logger log = Logger.getLogger(Reports.class);
	
	public void printRouteInfo(JsonResponseRoute route) { // for debugging
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
	
	public String printTripInfo(JsonTrip trip) { // for debugging				
		String result = "";
		result += 
		"modality: " + trip.getModality() + 
				" - Total Distance: " + trip.getDistanceMeter() + " meters" +
				" - Total Duration " + trip.getDurationMinutes() + " minutes" +
				" - Total Emissions " + trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2) + " CO2";			
		List<JsonSegment> segments = trip.getSegments();
		int k = 0;
		while (k < segments.size()) {
			result +=" Segment: " + k + ":";
			JsonSegment segment = segments.get(k);
			result += printSegmentInfo(segment);
			k++;
		}
		return result;
				
	}
	
	public String printSegmentInfo(JsonSegment segment) { // for debugging		
		String result = "";
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
			
			result += " mode of transport: " + segment.getType() + " - " + 
					"From: " + from +
					" To: " + to +
					" - distance: " + segment.getDistanceMeter() + " meters " + 
					" - duration: " + segment.getDurationMinutes() + " minutes ";
			
		}catch (Exception e){
			e.printStackTrace();
		}	
		return result;
	}
}