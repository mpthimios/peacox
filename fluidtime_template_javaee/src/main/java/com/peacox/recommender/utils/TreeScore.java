package com.peacox.recommender.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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

import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.repository.EmissionStatistics;
import com.peacox.recommender.repository.Stages;
import com.peacox.recommender.repository.StagesService;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserTreeScores;
import com.peacox.recommender.repository.UserTreeScoresService;
import com.peacox.recommender.webservice.Webservice;

@Component("TreeScore")
public class TreeScore{

	@Autowired
	private StagesService stagesService;
	
	@Autowired
	private UserTreeScoresService userTreeScoresService;
	
	protected Logger log = Logger.getLogger(Webservice.class);
	
    public double calculateStatic(Long userId){
	
    	double score = 0.0;
		
		UserTreeScores userTreeScore = userTreeScoresService.findUserTreeScore(userId);
		
		if (userTreeScore == null){
			//problem
			log.error("no score could be retrieved for user: " + userId);
			return score;
		}
    	
    	int numberOfDays = stagesService.findNumberOfDaysTraced(userId);
    	log.debug("number of days: " + stagesService.findNumberOfDaysTraced(userId));
    	
    	if (numberOfDays >=14) numberOfDays = 14;
    	else{
    		if (numberOfDays <= 7){
    			log.debug("number of days are less than 7");
    			return score;
    		}
    		else{
    			if (numberOfDays*1.0 % 2.0 != 0){
    				log.debug("number of days are odd");
        			return score;
    			}
    		}
    	}
    	// do something with the numberOfDays
    	double halfNumberOfDays = numberOfDays*1.0/2.0;
    	
		Calendar from = Calendar.getInstance();
		from.set(Calendar.HOUR_OF_DAY,0);
		from.set(Calendar.MINUTE,0);
		from.set(Calendar.SECOND,0);
		from.set(Calendar.MILLISECOND,0);
		
		Calendar to = Calendar.getInstance();
		to.set(Calendar.HOUR_OF_DAY,23);
		to.set(Calendar.MINUTE,59);
		to.set(Calendar.SECOND,59);
		to.set(Calendar.MILLISECOND,999);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd");
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
		
		LinkedHashMap<String, Double> dailyEmissions = new LinkedHashMap();
		LinkedHashMap<String, Double> dailyTravelDistance = new LinkedHashMap();
		double emissions7 = 0.0;
		double emissions7_14 = 0.0;
		double distance7 = 0.0;
		double distance7_14 = 0.0;
		
		for (int i = 0; i < numberOfDays; i++){
			if (i > 0){
				from.add(Calendar.DAY_OF_MONTH, -1);
				to.add(Calendar.DAY_OF_MONTH, -1);
			}
			List<Stages> stages = stagesService.findStagesByUserIdAndDate(userId, from.getTime(), to.getTime());				
			log.debug("number of stages found: " + stages.size());
			double dailyEmissionsValue = 0.0;
			double dailyTravelDistanceValue = 0.0;
			for(Stages stage : stages){
				log.debug("found stage: " + stage.getId() + " distance: " +stage.getDistance() +
						" mode: " + stage.getMode_detected_code());
				int mode = stage.getMode_detected_code();
				dailyEmissionsValue += AverageEmissions.getLarasAverageEmissions(mode) * (stage.getDistance()/1000.0);
				dailyTravelDistanceValue += stage.getDistance()/1000.0;
			}
			dailyEmissions.put(format.format(from.getTime()), dailyEmissionsValue);
			dailyTravelDistance.put(format.format(from.getTime()), dailyTravelDistanceValue);
			
			if (i*1.0 < halfNumberOfDays){
				emissions7 += dailyEmissionsValue;
				distance7 += dailyTravelDistanceValue;
			}
			else{
				emissions7_14 += dailyEmissionsValue;
				distance7_14 += dailyTravelDistanceValue;
			}
			
			log.debug("current day: " + format.format(from.getTime()) + " emissions: " +
					dailyEmissionsValue/dailyTravelDistanceValue);	
		}
		
		log.debug("average emissions_7: " + emissions7/distance7);
		log.debug("average emissions_7_14: " + emissions7_14/distance7_14);
		
		score = userTreeScore.getScore();
		
		log.debug("current score for user " + userId + " is: " + score); 
		
	
		//case 1: emission7 is 0-50grams/km -> user gains a point
		if ((distance7 > 0) && (emissions7/distance7 > 0 && emissions7/distance7 < 50))
			score += 3;
		
		else{
			//case 2: Get average emissions for seven days before that 
			//(i.e. if today is the 22nd, from the 8th to the 14th) 
			//and divide it by the total travel distance during that time frame (= “emission14”)
			//If emission7_14 < emission7 -> user gains a point
			if (distance7_14 > 0 && distance7 > 0){
				if ( emissions7_14/distance7_14 > emissions7/distance7)
					score += 3;
				else{
					//i. If emission7_14 >= emission-7 OR emission7 is 50-100 grams/km 
					//-> user gains no points
					if ( emissions7_14/distance7_14 <= emissions7/distance7 || 
							(emissions7/distance7 > 50 && emissions7/distance7 < 100))
						score = score; // do nothing
					else{
						score -= 3;
					}
				}
			}
		}
		
		userTreeScore.setScore(score);
		
		log.debug("new score: " + score);
		
		userTreeScoresService.update(userTreeScore);
    	return score;
    }
    
    public double calculateDynamic(Long userId){
    	
    	double score = 30.0;
    	double variance = 0.1;
		
		UserTreeScores userTreeScore = userTreeScoresService.findUserTreeScore(userId);
		
		
		if (userTreeScore == null){
			//problem
			log.error("no score could be retrieved for user: " + userId);
			return score;
		}
		
		score = userTreeScore.getScore();
    	
    	int numberOfDays = stagesService.findNumberOfDaysTraced(userId);
    	log.debug("number of days: " + stagesService.findNumberOfDaysTraced(userId));
    	
    	if (numberOfDays >=14) numberOfDays = 14;
    	else{
    		if (numberOfDays <= 7){
    			log.debug("number of days are less than 7");
    			return score;
    		}
    		else{
    			if (numberOfDays*1.0 % 2.0 != 0){
    				log.debug("number of days are odd");
        			return score;
    			}
    		}
    	}
    	// do something with the numberOfDays
    	double halfNumberOfDays = numberOfDays*1.0/2.0;
    	
		Calendar from = Calendar.getInstance();
		from.set(Calendar.HOUR_OF_DAY,0);
		from.set(Calendar.MINUTE,0);
		from.set(Calendar.SECOND,0);
		from.set(Calendar.MILLISECOND,0);
		
		Calendar to = Calendar.getInstance();
		to.set(Calendar.HOUR_OF_DAY,23);
		to.set(Calendar.MINUTE,59);
		to.set(Calendar.SECOND,59);
		to.set(Calendar.MILLISECOND,999);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy MM dd");
		
		LinkedHashMap<String, Double> dailyEmissions = new LinkedHashMap();
		LinkedHashMap<String, Double> dailyTravelDistance = new LinkedHashMap();
		double emissions7 = 0.0;
		double emissions7_14 = 0.0;
		double distance7 = 0.0;
		double distance7_14 = 0.0;
		
		for (int i = 0; i < numberOfDays; i++){
			if (i > 0){
				from.add(Calendar.DAY_OF_MONTH, -1);
				to.add(Calendar.DAY_OF_MONTH, -1);
			}
			List<Stages> stages = stagesService.findStagesByUserIdAndDate(userId, from.getTime(), to.getTime());				
			log.debug("number of stages found: " + stages.size());
			double dailyEmissionsValue = 0.0;
			double dailyTravelDistanceValue = 0.0;
			for(Stages stage : stages){				
				int mode = stage.getMode_detected_code();
				dailyEmissionsValue += AverageEmissions.getLarasAverageEmissions(mode) * (stage.getDistance()/1000.0);
				dailyTravelDistanceValue += stage.getDistance()/1000.0;
			}
			dailyEmissions.put(format.format(from.getTime()), dailyEmissionsValue);
			dailyTravelDistance.put(format.format(from.getTime()), dailyTravelDistanceValue);
			
			if (i*1.0 < halfNumberOfDays){
				emissions7 += dailyEmissionsValue;
				distance7 += dailyTravelDistanceValue;
			}
			else{
				emissions7_14 += dailyEmissionsValue;
				distance7_14 += dailyTravelDistanceValue;
			}
			
			log.debug("current day: " + format.format(from.getTime()) + " emissions: " +
					dailyEmissionsValue);	
		}
		
		//score = userTreeScore.getScore();
		
		log.debug("current score for user " + userId + " is: " + score); 
		
		//case 1: emission7 is 0-50grams/km -> user gains a point
		if (emissions7/distance7 > 0 && emissions7/distance7 < 50)
			score += 3;
		
		else{
			//case 2: Get average emissions for seven days before that 
			//(i.e. if today is the 22nd, from the 8th to the 14th) 
			//and divide it by the total travel distance during that time frame (= “emission14”)
			//If emission7_14 < emission7 -> user gains a point
			//Difference = emission7_14  - emission7
			//If difference > emission7*variance user gains a point
			if ( Math.abs(emissions7_14/distance7_14 - emissions7/distance7) > 
				(emissions7/distance7)*variance)
				score += 3;
			else{				
				if ( Math.abs(emissions7_14/distance7_14 - emissions7/distance7) < 
					(emissions7/distance7)*variance || 
					(emissions7/distance7 > 50 && emissions7/distance7 < 100))
					score = score; // do nothing
				else{
					score -= 3;
				}
			}
		}
		
		userTreeScore.setScore(score);
		
		log.debug("new score: " + score);
		
		userTreeScoresService.update(userTreeScore);
    	return score;
    }

}