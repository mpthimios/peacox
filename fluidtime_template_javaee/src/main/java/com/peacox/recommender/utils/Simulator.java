/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peacox.recommender.utils;

import com.fluidtime.routeExample.Route;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.GetRecommendationsRouteDto;
import com.peacox.recommender.UserPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jruby.RubyArray;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import java.util.ArrayList;
import java.util.Map;
import org.hibernate.Session;
import org.jruby.RubyHash;

/**
 *
 * @author mpthimios
 */

public class Simulator {
  public static void RunSimulation() throws Exception {
      
	ArrayList<RouteDto> routes = SimulatedRoutes.fetchRoutes();
	
    UserPreferences userPreferences= new UserPreferences();
    
    GetRecommendationsRouteDto recommendations = new GetRecommendationsRouteDto();
    LinkedHashMap<Integer, HashMap> finalRouteResults = recommendations.getRecommendations(userPreferences.getUserPreferences(), routes);
    
    System.out.println("**********Printing Routes**********");
    for (Integer key : finalRouteResults.keySet()){
        HashMap route = finalRouteResults.get(key);
        Map.Entry<RouteDto, Double> entry = (Map.Entry<RouteDto, Double>) route.entrySet().iterator().next();
        System.out.println("\tRoute " + entry.getKey().getId() + " Utility: " + entry.getValue());
        
        for (TripDto trip : entry.getKey().getTrips()){
            System.out.println("\t\tTrip: " + trip.getId() + " MOT: " + trip.getModality() + " duration: " + trip.getDurationMinutes());        
        }
        
    }
    
    System.out.println("**********END Printing Routes**********");
    
  }
}