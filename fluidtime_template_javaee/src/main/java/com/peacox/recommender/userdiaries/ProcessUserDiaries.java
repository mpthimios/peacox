package com.peacox.recommender.userdiaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.LinkedList;
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
import com.fluidtime.library.webservice.model.json.JsonTrip;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.repository.EmissionStatistics;
import com.peacox.recommender.repository.RecommendationDetails;
import com.peacox.recommender.repository.RecommendationDetailsService;
import com.peacox.recommender.repository.Stages;
import com.peacox.recommender.repository.StagesService;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserRouteRequest;
import com.peacox.recommender.repository.UserTreeScores;
import com.peacox.recommender.repository.UserTreeScoresService;
import com.peacox.recommender.utils.CompressString;
import com.peacox.recommender.webservice.Webservice;



@Component("ProcessUserDiaries")
public class ProcessUserDiaries{
	protected Logger log = Logger.getLogger(ProcessUserDiaries.class);
	
	@Autowired
	private RecommendationDetailsService recommendationDetailsService;
	
	public String createDiaryGraph(String fileName){
		LinkedHashMap<Integer, List<UserDiary>> userDiaries = loadFromFile(fileName);
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		int totalEntries = 0;
		int totalEntriesWithPeacox = 0;
		int totalEntriesWithoutPeacox = 0;
		StringBuffer buffer = new StringBuffer();
		
		for(Map.Entry<Integer, List<UserDiary>> userDiaryEntries : userDiaries.entrySet()){
			log.debug("processing user: " + userDiaryEntries.getKey());
			int routeWithCarAndPeacox = 0;
			int routeWithCarWithoutPeacox = 0;
			Date lastDate = null;
			int numEntriesWithPeacox = 0;
			for (UserDiary userDiaryEntry : userDiaryEntries.getValue()){				
				if (true){//userDiaryEntry.getUsedRoutePlanner() == 1){
					numEntriesWithPeacox++;
					totalEntriesWithPeacox++;
					if (userDiaryEntry.getTransportMode().matches("car")){
						routeWithCarAndPeacox++;
					}
					if (userDiaryEntry.getStartDateTime() != null){
						//need to print some stuff
						
						buffer.append("" + userDiaryEntry.getUserId());						
						buffer.append("\t");
						buffer.append("" + userDiaryEntry.getComment());						
						buffer.append("\t");
						buffer.append("" + userDiaryEntry.getUsedRoutePlanner());
						buffer.append("\t");
						buffer.append("" + sdf.format(userDiaryEntry.getStartDateTime()));
						buffer.append("\t");
						buffer.append("" + userDiaryEntry.getTransportMode());
						buffer.append("\t");
						buffer.append("" + userDiaryEntry.getAvgSpeed());
						buffer.append("\t");
						buffer.append("" + userDiaryEntry.getDistance());
						buffer.append("\t");
						buffer.append("" + userDiaryEntry.getDuration());
						try {
							buffer.append("\t");
							buffer.append("" + userDiaryEntry.getRecommendationDetails().getUser_id());
							String decommpressedRecommendation = CompressString.decompress(userDiaryEntry.getRecommendationDetails().getRecommendations());
							buffer.append("\t");
							buffer.append("" + sdf.format(userDiaryEntry.getRecommendationDetails().getUserRouteRequest().getTimestamp()));
							JsonResponseRoute routeResponse = RouteParser
					                .routeFromJson(decommpressedRecommendation);
							buffer.append("\t");
							for (com.fluidtime.library.model.json.JsonTrip trip : routeResponse.getTrips()){
								buffer.append("\t");
								buffer.append("Recommender Trip Start");
								buffer.append("\t");
								buffer.append(""+trip.getModality());
								buffer.append("\t");
								buffer.append(""+trip.getDistanceMeter());
								buffer.append("\t");
								buffer.append(""+trip.getDurationMinutes());
								buffer.append("\t");
								buffer.append(""+trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
								buffer.append("\t");
								buffer.append("Segments Start");
								buffer.append("\t");
								for(com.fluidtime.library.model.json.JsonSegment segment : trip.getSegments()){
									buffer.append(""+segment.getType());
									buffer.append("\t");
									buffer.append(""+segment.getDistanceMeter());
									buffer.append("\t");
									buffer.append(""+segment.getDurationMinutes());
									buffer.append("\t");
									buffer.append(""+segment.getSpeedMeterPerMinute());
									buffer.append("\t");
									buffer.append(""+segment.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
									buffer.append("\t");
								}
								buffer.append("Segments End");
								buffer.append("\t");
								buffer.append("Recommender Trip End");
							}
						}
						catch(Exception e){
							
						}
						try {
							String decommpressedRoute = CompressString.decompress(
									userDiaryEntry.getRecommendationDetails().getUserRouteResult().getResult());
							buffer.append("\t");
							buffer.append("" + sdf.format(userDiaryEntry.getRecommendationDetails().getUserRouteRequest().getTimestamp()));
							JsonResponseRoute routeResponse = RouteParser
					                .routeFromJson(decommpressedRoute);
							buffer.append("\t");
							for (com.fluidtime.library.model.json.JsonTrip trip : routeResponse.getTrips()){
								buffer.append("\t");
								buffer.append("Route Result Trip Start");
								buffer.append("\t");
								buffer.append(""+trip.getModality());
								buffer.append("\t");
								buffer.append(""+trip.getDistanceMeter());
								buffer.append("\t");
								buffer.append(""+trip.getDurationMinutes());
								buffer.append("\t");
								buffer.append(""+trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
								buffer.append("\t");
								buffer.append("Segments Start");
								buffer.append("\t");
								for(com.fluidtime.library.model.json.JsonSegment segment : trip.getSegments()){
									buffer.append(""+segment.getType());
									buffer.append("\t");
									buffer.append(""+segment.getDistanceMeter());
									buffer.append("\t");
									buffer.append(""+segment.getDurationMinutes());
									buffer.append("\t");
									buffer.append(""+segment.getSpeedMeterPerMinute());
									buffer.append("\t");
									buffer.append(""+segment.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
									buffer.append("\t");
								}
								buffer.append("Segments End");
								buffer.append("\t");
								buffer.append("Route Result Trip End");
							}
						}
						catch(Exception e){
							
						}
						buffer.append("\n");
						
					}					
				}
				else{
					totalEntriesWithoutPeacox++;
				}
				totalEntries++;
			}
			log.debug("numEntriesWithPeacox: " + numEntriesWithPeacox);			
		}
		log.debug(buffer.toString());
		log.debug("totalEntries: " + totalEntries);
		log.debug("totalEntriesWithPeacox: " + totalEntriesWithPeacox);
		log.debug("totalEntriesWithoutPeacox: " + totalEntriesWithoutPeacox);
		return buffer.toString();
	}
	
	public String fetchRecommendations(){
		
		StringBuffer buffer = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		List<RecommendationDetails> recommendationDetailsList = 
				recommendationDetailsService.getAll();
		
		for (RecommendationDetails recommendationDetail : recommendationDetailsList){
			try {
				buffer.append("\t");
				buffer.append("" + recommendationDetail.getUser_id());
				String decommpressedRecommendation = CompressString.decompress(recommendationDetail.getRecommendations());
				buffer.append("\t");
				buffer.append("" + sdf.format(recommendationDetail.getUserRouteRequest().getTimestamp()));
				JsonResponseRoute routeResponse = RouteParser
		                .routeFromJson(decommpressedRecommendation);
				buffer.append("\t");
//				buffer.append("" + routeResponse.getLocationFrom().toString());
//				buffer.append("\t");
//				buffer.append("" + routeResponse.getLocationTo().toString());
//				buffer.append("\t");
				for (com.fluidtime.library.model.json.JsonTrip trip : routeResponse.getTrips()){
					buffer.append("\t");
					buffer.append("Recommender Trip Start");
					buffer.append("\t");
					buffer.append(""+trip.getModality());
					buffer.append("\t");
					buffer.append(""+trip.getDistanceMeter());
					buffer.append("\t");
					buffer.append(""+trip.getDurationMinutes());
					buffer.append("\t");
					buffer.append(""+trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
					buffer.append("\t");
//					buffer.append("Segments Start");
//					buffer.append("\t");
//					for(com.fluidtime.library.model.json.JsonSegment segment : trip.getSegments()){
//						buffer.append(""+segment.getType());
//						buffer.append("\t");
//						buffer.append(""+segment.getDistanceMeter());
//						buffer.append("\t");
//						buffer.append(""+segment.getDurationMinutes());
//						buffer.append("\t");
//						buffer.append(""+segment.getSpeedMeterPerMinute());
//						buffer.append("\t");
//						buffer.append(""+segment.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
//						buffer.append("\t");
//					}
//					buffer.append("Segments End");
//					buffer.append("\t");
					buffer.append("Recommender Trip End");
				}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				try {
//					String decommpressedRoute = CompressString.decompress(
//							recommendationDetail.getUserRouteResult().getResult());
//					buffer.append("\t");
//					buffer.append("" + sdf.format(recommendationDetail.getUserRouteRequest().getTimestamp()));
//					JsonResponseRoute routeResponse = RouteParser
//			                .routeFromJson(decommpressedRoute);
//					buffer.append("\t");
//					for (com.fluidtime.library.model.json.JsonTrip trip : routeResponse.getTrips()){
//						buffer.append("\t");
//						buffer.append("Route Result Trip Start");
//						buffer.append("\t");
//						buffer.append(""+trip.getModality());
//						buffer.append("\t");
//						buffer.append(""+trip.getDistanceMeter());
//						buffer.append("\t");
//						buffer.append(""+trip.getDurationMinutes());
//						buffer.append("\t");
//						buffer.append(""+trip.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
//						buffer.append("\t");
//						buffer.append("Segments Start");
//						buffer.append("\t");
//						for(com.fluidtime.library.model.json.JsonSegment segment : trip.getSegments()){
//							buffer.append(""+segment.getType());
//							buffer.append("\t");
//							buffer.append(""+segment.getDistanceMeter());
//							buffer.append("\t");
//							buffer.append(""+segment.getDurationMinutes());
//							buffer.append("\t");
//							buffer.append(""+segment.getSpeedMeterPerMinute());
//							buffer.append("\t");
//							buffer.append(""+segment.getAttribute(AttributeListKeys.KEY_SEGMENT_CO2));
//							buffer.append("\t");
//						}
//						buffer.append("Segments End");
//						buffer.append("\t");
//						buffer.append("Route Result Trip End");
//					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				buffer.append("\n");
		}
		
		return buffer.toString();
	}
	
public String fetchRequests(){
		
		StringBuffer buffer = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		List<RecommendationDetails> recommendationDetailsList = 
				recommendationDetailsService.getAll();
		
		for (RecommendationDetails recommendationDetail : recommendationDetailsList){
			try {
				buffer.append("\t");
				buffer.append("" + recommendationDetail.getUser_id());
				
				UserRouteRequest routeRequest = recommendationDetail.getUserRouteRequest();
				
				RequestGetRoute request = RouteParser.routeRequestFromJson(routeRequest.getRequest());
				
				String decommpressedRecommendation = CompressString.decompress(recommendationDetail.getRecommendations());
				buffer.append("\t");
				buffer.append("" + sdf.format(recommendationDetail.getUserRouteRequest().getTimestamp()));
				JsonResponseRoute routeResponse = RouteParser
		                .routeFromJson(decommpressedRecommendation);
				buffer.append("\t");
				String modalities = "";
				for (String modality : request.getModality()){
					modalities += "|" + modality;
				}
				buffer.append(modalities);
				buffer.append("\t");
				buffer.append(request.getDetails().getAppName());
				buffer.append("\t");
			}
			catch(Exception e){
				e.printStackTrace();
			}				
			buffer.append("\n");
		}
		
		return buffer.toString();
	}
	
	public LinkedHashMap loadFromFile(String fileName){
		BufferedReader reader = null;
		LinkedHashMap<Integer, List> userDiaries = new LinkedHashMap();		
		try {
			File inputFile = new File(fileName);
			reader = new BufferedReader(new InputStreamReader(ProcessUserDiaries.class.
			    	getClassLoader().
			    	getResourceAsStream(fileName)));
			
			String line = "";
			line = reader.readLine();
			int lineCount = 0;
			int lineCountProcessed = 0;
			int lineCountNotProcessed = 0;
			while ((line = reader.readLine()) != null) {
				lineCount++;
		        //have to read diary stuff
				String[] lineData = line.split("\t");
				if (lineData.length >= 10){
					lineCountProcessed++;
					if (userDiaries.containsKey(Integer.parseInt(lineData[1]))){
						List<UserDiary> diaryEntries = userDiaries.get(Integer.parseInt(lineData[1]));
						UserDiary userDiary = fillData(lineData);
						diaryEntries.add(userDiary);
						userDiaries.put(userDiary.getUserId(), diaryEntries);
					}
					else{
						List<UserDiary> diaryEntries = new LinkedList<UserDiary>();
						UserDiary userDiary = fillData(lineData);
						diaryEntries.add(userDiary);
						userDiaries.put(userDiary.getUserId(), diaryEntries);
					}
				}
				else{
					lineCountNotProcessed++;
				}
		    }
			log.debug("lineCount: " + lineCount);
			log.debug("lineCountProcessed: " + lineCountProcessed);
			log.debug("lineCountNotProcessed: " + lineCountNotProcessed);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try {
		        if (reader != null) {
		            reader.close();
		        }
		    } 
			catch (IOException e) {
		    }
		}
		
		return userDiaries;
	}
	
	private UserDiary fillData(String[] lineData){
		try{
			log.debug("size of line: " + lineData.length);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			UserDiary userDiary = new UserDiary();
			userDiary.setPersonCode(lineData[0]);
			userDiary.setUserId(Integer.parseInt(lineData[1]));
			userDiary.setTripNrOverall(Integer.parseInt(lineData[2]));
			userDiary.setTripNrPerPerson(Integer.parseInt(lineData[3]));
			Calendar cal = Calendar.getInstance();
			try{
				cal.setTime(sdf.parse(lineData[4] + " " + lineData[5]));
				userDiary.setStartDateTime(cal.getTime());				
			}catch(java.text.ParseException e){
				userDiary.setStartDateTime(null);
			}
			try{
				cal.setTime(sdf.parse(lineData[4] + " " + lineData[6]));
				userDiary.setEndDateTime(cal.getTime());
			}catch(java.text.ParseException e){
				userDiary.setEndDateTime(null);
			}
			userDiary.setTransportMode(lineData[7]);
			userDiary.setTripPurpose(lineData[8]);
			userDiary.setUsedRoutePlanner(Integer.parseInt(lineData[9]));
			if (lineData.length >= 11){
				userDiary.setComment(lineData[10]);
			}
			
			if ((userDiary.getUsedRoutePlanner() == 1 || userDiary.getUsedRoutePlanner() == -1) && userDiary.getStartDateTime() != null
					&& userDiary.getUserId() > 10){
				RecommendationDetails recommendationDetails = 
						recommendationDetailsService.getFirstBeforeDate(userDiary.getStartDateTime(), userDiary.getUserId());
				userDiary.setRecommendationDetails(recommendationDetails);
			}
			else{
				userDiary.setRecommendationDetails(null);
			}
			
			if (lineData.length > 13){
				try{
					userDiary.setDistance(Double.parseDouble(lineData[13]));
				}catch (NumberFormatException e){
					log.debug("could not read distance");
				}
			}
			if (lineData.length > 14){
				try{
					userDiary.setDuration(Double.parseDouble(lineData[14]));
				}catch (NumberFormatException e){
					log.debug("could not read duration");
				}
			}
			if (lineData.length > 15){
				try{
					userDiary.setAvgSpeed(Double.parseDouble(lineData[15]));
				}catch (NumberFormatException e){
					log.debug("could not read average speed");
				}
			}
			
			return userDiary;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}		
	}
    
}