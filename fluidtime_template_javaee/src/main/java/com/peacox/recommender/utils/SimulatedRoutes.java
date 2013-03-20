/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.peacox.recommender.utils;

import com.fluidtime.routeExample.Route;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.GetRecommendations;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.util.ArrayList;
import java.util.Map;
import org.hibernate.Session;
import org.jruby.RubyHash;

/**
 *
 * @author mpthimios
 */

public class SimulatedRoutes {
  public static void main(String args[]) throws Exception {
      
	ArrayList<RouteDto> routes = SimulatedRoutes.fetchRoutes();
	
    UserPreferences userPreferences= new UserPreferences();
    
    GetRecommendations recommendations = new GetRecommendations();
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
  
  public static ArrayList<RouteDto> fetchRoutes(){
	 
	  //get jruby engine
	    ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
	    ArrayList<RouteDto> routes = null;
	    //process a ruby file    
	    try {	    	
			jruby.eval(new BufferedReader(new InputStreamReader(SimulatedRoutes.class.
			    	getClassLoader().
			    	getResourceAsStream("random_routes.rb"))));
					//ClassLoader.getSystemResourceAsStream("random_routes.rb"))));
		    //call a method defined in the ruby source
//		    jruby.put("number", 6);
//		    jruby.put("title", "My Swing App");
//		    long fact = (Long) jruby.eval("showFactInWindow($title,$number)");
//		    System.out.println("fact: " + fact);  
		    jruby.put("how_many", 6);
		    RubyArray result = (RubyArray) jruby.eval("generate_routes($how_many)");
		    System.out.println("ruby result: " + result);  
		    
		    routes = parseRoutes(result);
		    
		    for (RouteDto route : routes){
		        System.out.println(" route found");
		        for (TripDto trip : route.getTrips()){            
		            System.out.println(" duration: " + trip.getDurationMinutes());
		            System.out.println(" modality: " + trip.getModality());
		            System.out.println(" emissions: " + trip.getDescription());             
		        }
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("something really bad happened");
			e.printStackTrace();
		}
	    return routes;
  }
  
  public static void getRoutes(){
      Route route = new Route();
      RouteDto routeDto = route.getRouteExample();
      
      //print some info for the route:
      System.out.println("from: " + routeDto.getLocationFrom().getTitle());
      System.out.println("to: " + routeDto.getLocationTo().getTitle());
      List<TripDto> trips = routeDto.getTrips();      
      for (Iterator it = trips.iterator(); it.hasNext();){
          TripDto trip = (TripDto) it.next();
          System.out.println("tripId: " +  trip.getId() 
                  + " MoT: " + trip.getModality()
                  + " duration: " + trip.getDurationMinutes()
                  + " emissions: " + trip.getDescription());          
      }
  }
  
  public static ArrayList parseRoutes(RubyArray routes){
      
//       Session hSession = HibernateUtil.getSessionFactory().openSession();
//       hSession.getTransaction().begin();
//       OwnedVehicles vehicles = (OwnedVehicles) hSession
//                .createQuery("select v from OwnedVehicles v where v.userId = :uid")
//                .setParameter("uid", 1)
//                .uniqueResult();
//	hSession.getTransaction().commit();
        
//        System.out.println("user avatar name: " + user.getAvatarName());
       // System.out.println("user ownedVehicle: " + vehicles.getType());
        
//        boolean userOwnsCar = false;
//        boolean userOwnsBicycle = false;
//        
//        if(vehicles.getType().matches("car")){
//            userOwnsCar = true;
//        }
//        if(vehicles.getType().matches("bicycle")){
//            userOwnsBicycle = true;
//        }
//      
      ArrayList result = new ArrayList();
      
      int i = 0;
      for (Object element: routes){
          RouteDto routeDto = new RouteDto();
          routeDto.setId((long)i);
          RubyArray rubyTrips = (RubyArray)element;
          List<TripDto> trips = new ArrayList<TripDto>();
          List<String> mots = new ArrayList<String>();
          int j = 0;
          for (Object trip : rubyTrips){
              RubyHash rubyHashElement = (RubyHash) trip;
              TripDto tripDto = new TripDto();
              tripDto.setId((long) j);
              tripDto.setModality((String)rubyHashElement.get("mot"));
              mots.add((String)rubyHashElement.get("mot"));
              tripDto.setDurationMinutes(((Double) rubyHashElement.get("duration")).intValue());
              tripDto.setDescription(((Double) rubyHashElement.get("emissions")).toString());
              trips.add(tripDto);
              j++;
          }
          i++;
          routeDto.setTrips(trips);
          boolean includeRoute = true;
//          if (mots.contains("car") && !userOwnsCar){
//              includeRoute = false;
//              System.out.println("should not include a car");
//          }
//          
//          if (mots.contains("bicycle") && !userOwnsBicycle){
//              includeRoute = false;
//              System.out.println("should not include a bicycle");
//          }
          
          if (includeRoute){
            result.add(routeDto);
          }
      }      
      return result;      
  }
  
}
