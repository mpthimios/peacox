package com.peacox.recommender;

import com.fluidtime.library.model.json.JsonSegment;
import com.fluidtime.library.model.json.JsonTrip;
import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.fluidtime.library.model.json.FeatureTypes.JsonFeature;
import com.fluidtime.library.model.json.response.route.JsonResponseRoute;
import com.fluidtime.brivel.route.json.response.JsonResponseRouteTrip;
//import com.peacoxrmi.model.User;
import de.bezier.math.combinatorics.Combination;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;

public class GetRecommendations{
    
  //Stats
  private LinkedHashMap maxValues = new LinkedHashMap<String, Double>();
  private LinkedHashMap minValues = new LinkedHashMap<String, Double>();
  private LinkedHashMap sumValues = new LinkedHashMap<String, Double>();
  
  private double maxValue = 0;
  private double minValue = 1000000;
  private double sumValue = 0;
  
  private String ownsVehicle = "";
  
  public LinkedHashMap getRecommendations(LinkedHashMap userPreferences, ArrayList<JsonResponseRoute> routeResults){
        LinkedHashMap finalRouteResults;
        updateTotalDurationStats(routeResults);
        updateTotalWBDurationStats(routeResults);
        updateTotalEmissionStats(routeResults);
        
        switch(((Double)userPreferences.get("orderAlgorithm")).intValue()){
            case 1: finalRouteResults = methodForRecommendations1(userPreferences, routeResults);
                break;
            case 2: finalRouteResults = methodForRecommendations2(userPreferences, routeResults);
                break;                                                                          
            default: finalRouteResults = methodForRecommendations1(userPreferences, routeResults);
                break;                                        
        }
        return finalRouteResults;
    }
    
    private LinkedHashMap methodForRecommendations1(LinkedHashMap userPreferences, 
        ArrayList<JsonResponseRoute> routeResults){
    	
    	System.out.println("DEBUG: methodForRecommendations1");
        
    	ArrayList tripsList = new ArrayList<JsonTrip>();
    	
    	for(JsonResponseRoute route : routeResults){
      	  for(JsonTrip trip : route.getTrips()){
      		tripsList.add(trip);
      	  }
      	}
    	
        Combination combination = new Combination(tripsList.size(), tripsList.size());//3);
        //ArrayList<String> myArr = new ArrayList<String>();
        LinkedHashMap combinationsAndUtilities = new LinkedHashMap<ArrayList<JsonTrip>, Double>();
        HashMap tripsAndUtilities = new HashMap<JsonTrip, Double>();
        double howMany = 0.0;
        try{ 
            while (combination.hasMore()){
                    int[] combi = combination.next();
                    System.out.println("new combination found");
                    double utility = 0.0;
                    ArrayList<Integer> myArr = new ArrayList<Integer>();

                    double totalDuration = 0.0;
                    for (int temp = 0; temp < combi.length; temp++){
                        System.out.println(" combi: " + combi[temp]);
                        int pos = combi[temp];
                        myArr.add(pos);
                        JsonTrip tripResult = (JsonTrip) tripsList.get(pos);
                        double tripUtility = 0.0;
                        switch(((Double)userPreferences.get("utilityAlgorithm")).intValue()){
                            case 0: tripUtility += routeUtilityCalulation(tripResult, userPreferences);                                        
                                break;
                            case 1: tripUtility += routeUtilityCalulation(tripResult, userPreferences);                                        
                                break;
                            case 2: tripUtility += routeUtilityCalulationP2(tripResult, userPreferences);
                                break;
                            case 3: tripUtility += routeUtilityCalulationP3(tripResult, userPreferences);
                                break;
                            case 4: tripUtility += routeUtilityCalulationP4(tripResult, userPreferences);
                                break;
                            case 5: tripUtility += routeUtilityCalulationP5(tripResult, userPreferences);
                            break;
                            default: tripUtility += routeUtilityCalulation(tripResult, userPreferences);
                                break;                                        
                        }
                        utility += tripUtility;
                        if (!tripsAndUtilities.containsKey(tripResult)){
                        	tripsAndUtilities.put(tripResult, tripUtility);
                        }
                        //utility += routeUtilityCalulation(routeResult, userPreferences);
                        totalDuration += (double)getTripTotalDuration(tripResult);
                    }                    
                    combinationsAndUtilities.put(myArr, utility); //howMany should be replaced by utility
                    System.out.println("Route List no: " + howMany + " - utility: " + utility);
                    combinationsAndUtilities.put(myArr, utility);
                    howMany++;
            }
        }
        catch(Exception e){
                e.printStackTrace();	
        }
        
        System.out.println("combinationsAndUtilities size: " + combinationsAndUtilities.size());
        System.out.println("number of routes fetched: " + routeResults.size());

        ArrayList<Double> combinationsAndUtilitiesArray = new ArrayList<Double>(combinationsAndUtilities.values());
        Double maxValue = Collections.max(combinationsAndUtilitiesArray);
        System.out.println("max value utility: " + maxValue);	
        int  index = combinationsAndUtilitiesArray.indexOf(maxValue);
        System.out.println("index of list with max value: " + index);
        
        LinkedHashMap finalTripResults = new LinkedHashMap<Integer, HashMap>();

        ArrayList finalTrips = null;
        System.out.println("before iterating routes");
        int itCounter = 0;
        Iterator it=combinationsAndUtilities.keySet().iterator();
        while ( it.hasNext()) {
                if (itCounter == index){
                        finalTrips = (ArrayList)it.next();
                }
                else{
                        it.next();
                }
                itCounter++;
        }
        
        System.out.println("after iterating routes");
        //(Integer[])(new ArrayList<Integer[]>(combinationsAndUtilities.keySet())).get(index);

        System.out.println("before finding the route");
        //a variable to hold the routes already in the list
        ArrayList<Integer> topListValues = new ArrayList<Integer>();
        int tripCounter = 0;
        for (int temp = 0; temp < finalTrips.size(); temp++){
                JsonTrip trip = (JsonTrip) (tripsList.get((Integer)finalTrips.get(temp)));
                HashMap tripWithUtility = new HashMap<JsonTrip, Double>();
                tripWithUtility.put(trip, tripsAndUtilities.get(trip));
                finalTripResults.put(tripCounter,tripWithUtility);
                topListValues.add((Integer)finalTrips.get(temp));
                tripCounter++;
        }
        
        //sort finalTripResults based on utility value
        List<Map.Entry<Integer, HashMap>> intermediaryEntries =
    		  new ArrayList<Map.Entry<Integer, HashMap>>(finalTripResults.entrySet());
    		Collections.sort(intermediaryEntries, new Comparator<Map.Entry<Integer, HashMap>>() {
    		  public int compare(Map.Entry<Integer, HashMap> a, Map.Entry<Integer, HashMap> b){
    			  Double aValue = ((Double)(((Map.Entry<JsonTrip,Double>)(a.getValue().entrySet().iterator()
      		    		.next())).getValue()));
    			  Double bValue = ((Double)(((Map.Entry<JsonTrip,Double>)(b.getValue().entrySet().iterator()
        		    		.next())).getValue()));
    			  return bValue.compareTo(aValue);
    		  }
    		});
    		finalTripResults.clear();
    		
    		for (Map.Entry<Integer, HashMap> entry : intermediaryEntries) {
    			finalTripResults.put(entry.getKey(), entry.getValue());
    		}
        
        //Iterate routeResults and add the remainding results to the final routes
        //temporarily skip this
//        for (int temp = 0; temp < routeResults.size(); temp++){
//            if (!topListValues.contains(temp)){
//            	JsonTrip trip = (JsonTrip) ((JsonTrip) (tripsList.get(temp)));
//                HashMap tripWithUtility = new HashMap<JsonTrip, Double>();
//                tripWithUtility.put(trip, tripsAndUtilities.get(trip));
//                finalTripResults.put(tripCounter,tripWithUtility);               
//                tripCounter++;
//            }
//        }
        System.out.println("after finding the route");
             
        //Print results to check if we have UTF-8
        Collection c = finalTripResults.values();
        Iterator itr = c.iterator();
        PrintWriter outUTF8 = new PrintWriter(new OutputStreamWriter(System.out));
        int ci = 1;
      
        return finalTripResults;
  }
   
  private LinkedHashMap methodForRecommendations2(LinkedHashMap userPreferences, 
		  ArrayList<JsonResponseRoute> routeResults){
        
	  System.out.println("DEBUG: methodForRecommendations2");
	  
	  	ArrayList tripsList = new ArrayList<JsonTrip>();
  	
  		for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
    		tripsList.add(trip);
    	  }
    	}
	  
        //ArrayList<String> myArr = new ArrayList<String>();
        LinkedHashMap resultsAndUtilities = new LinkedHashMap<JsonTrip, Double>();
        
        Iterator iterator = tripsList.iterator();
        while ( iterator.hasNext() ){
        	JsonTrip tripResult = (JsonTrip) iterator.next();                        
        
            double howMany = 0.0;
            try{ 
                boolean writeToFile = true;
                String osName = System.getProperty("os.name");
                if (!osName.matches("(?i:.*windows.*)")){
                    writeToFile = true;
                }

                double utility = 0.0;
                ArrayList<Integer> myArr = new ArrayList<Integer>();

                double totalDuration = 0.0;

                switch(((Integer)userPreferences.get("utilityAlgorithm")).intValue()){
                    case 0: utility += routeUtilityCalulation(tripResult, userPreferences);
                        break;
                    case 1: utility += routeUtilityCalulationP1(tripResult, userPreferences);
                        break;
                    case 2: utility += routeUtilityCalulationP2(tripResult, userPreferences);
                        break;
                    case 3: utility += routeUtilityCalulationP3(tripResult, userPreferences);
                        break;
                    case 4: utility += routeUtilityCalulationP4(tripResult, userPreferences);
                        break;                                        
                    default: utility += routeUtilityCalulation(tripResult, userPreferences);
                        break;                                        
                }
                //utility += routeUtilityCalulation(routeResult, userPreferences);
                totalDuration += (double)this.getTripTotalDuration(tripResult);
                    
                resultsAndUtilities.put(tripResult, utility);
                howMany++;
            }
            catch(Exception e){
                    e.printStackTrace();	
            }
        }	
        
        System.out.println("number of routes fetched: " + resultsAndUtilities.size());

        LinkedHashMap finalTripResults = new LinkedHashMap<Integer, JsonTrip>();

        LinkedHashMap tmpFinalTripResults = null; //GetRoutes.sortByValue(resultsAndUtilities);
        Iterator iterator1 = tmpFinalTripResults.keySet().iterator();
        int position = 0;
        
        System.out.println("++++++ Going to Print Ordered Utilities: ++++++");
        System.out.println(" resultsAndUtilities size: " + resultsAndUtilities.size());
        System.out.println("tmpFinalRouteResults size: " + tmpFinalTripResults.size());
        while ( iterator1.hasNext() ){
        	JsonResponseRoute key = ( JsonResponseRoute ) iterator1.next();
            finalTripResults.put(position, key);
            System.out.println("position: " + position + " utility: " + (Double)tmpFinalTripResults.get(key)
                    + " totalDuration: " + "key.getTotalDuration()" 
                    + " wbDuration: " + "key.getWBDuration()");
            position++;
        }
        
      return finalTripResults;
  }
  
   public double routeUtilityCalulation(JsonTrip tripResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.get("duration10min");
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.get("duration30min");
	}
	else{
		durationCriterionValue = (Double)userPreferences.get("duration30plus");
	}
	
	double wbDuration = (double) getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.get("WB10min");
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.get("WB30min");
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.get("WB30plus");
	}
		
	totalUtility = (1/totalDuration)* 
					durationCriterionValue*
					((Double)userPreferences.get("durationImportance")) 
					+ 
					(1/wbDuration)* 
					wbDurationCriterionValue*
					((Double)userPreferences.get("wbtimeImportance"));
	
	return totalUtility;
  }

  public double routeUtilityCalulationP1(JsonTrip tripResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.get("duration10min");
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.get("duration30min");
	}
	else{
		durationCriterionValue = (Double)userPreferences.get("duration30plus");
	}
	
	double wbDuration = (double) getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.get("WB10min");
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.get("WB30min");
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.get("WB30plus");
	}
		
	totalUtility = (totalDuration/(Double)maxValues.get("maxTotalDuration"))* 
						durationCriterionValue*
						((Double)userPreferences.get("durationImportance")) 
						+ 
						(wbDuration/(Double)maxValues.get("maxWBTotalDuration"))* 
						wbDurationCriterionValue*
						((Double)userPreferences.get("wbtimeImportance"));
	
	return totalUtility;
  }
  
  public double routeUtilityCalulationP2(JsonTrip tripResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.get("duration10min");
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.get("duration30min");
	}
	else{
		durationCriterionValue = (Double)userPreferences.get("duration30plus");
	}
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.get("WB10min");
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.get("WB30min");
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.get("WB30plus");
	}
		
	totalUtility = ((totalDuration - (Double)minValues.get("minTotalDuration"))/
                                    ((Double)maxValues.get("maxTotalDuration") - (Double)minValues.get("minTotalDuration")))*
                                                durationCriterionValue*
						((Double)userPreferences.get("durationImportance")) 
						+ 
						((wbDuration - (Double)minValues.get("minWBTotalDuration"))/
                                    ((Double)maxValues.get("maxWBTotalDuration") - (Double)minValues.get("minWBTotalDuration")))*                                                
						wbDurationCriterionValue*
						((Double)userPreferences.get("wbtimeImportance"));
	
	return totalUtility;
  }
  
  public double routeUtilityCalulationP3(JsonTrip tripResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getTripTotalDuration(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.get("duration10min");
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.get("duration30min");
	}
	else{
		durationCriterionValue = (Double)userPreferences.get("duration30plus");
	}
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.get("WB10min");
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.get("WB30min");
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.get("WB30plus");
	}
		
	totalUtility = (totalDuration/(Double)sumValues.get("sumTotalDuration"))* 
						durationCriterionValue*
						((Double)userPreferences.get("durationImportance")) 
						+ 
						(wbDuration/(Double)sumValues.get("sumWBTotalDuration"))* 
						wbDurationCriterionValue*
						((Double)userPreferences.get("wbtimeImportance"));
	
	return totalUtility;
  }
  
  public double routeUtilityCalulationP4(JsonTrip tripResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) this.getTripTotalDuration(tripResult);
        double totalEmissions = this.getTripTotalEmissions(tripResult);
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.get("duration10min");
	} 
	else if (10 < totalDuration && totalDuration <= 30){
		durationCriterionValue = (Double)userPreferences.get("duration30min");
	}
	else{
		durationCriterionValue = (Double)userPreferences.get("duration30plus");
	}
        
    int numberOfChanges = tripResult.getSegments().size();
    double comfortCriterionValue = 0;
    
    if (numberOfChanges <= 2){
        comfortCriterionValue = (Double) userPreferences.get("comfortHigh");
    }
    else if (numberOfChanges > 2 && numberOfChanges <= 3){
        comfortCriterionValue = (Double) userPreferences.get("comfortMedium");
    }
    else {
        comfortCriterionValue = (Double) userPreferences.get("comfortLow");
    }
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration <= 10){
		wbDurationCriterionValue = (Double)userPreferences.get("WB10min");
	} 
	else if (10 < wbDuration && wbDuration <= 30){
		wbDurationCriterionValue = (Double)userPreferences.get("WB30min");
	}
	else{
		wbDurationCriterionValue = (Double)userPreferences.get("WB30plus");
	}
		
	totalUtility = durationCriterionValue*
						((Double)userPreferences.get("durationImportance")) 
						+ 
						wbDurationCriterionValue*
						((Double)userPreferences.get("wbtimeImportance"))
                                                +
                                                comfortCriterionValue*
                				((Double)userPreferences.get("comfortImportance"))
                                                ;
        
        if (false){
            totalUtility = totalUtility * totalEmissions/((Double)sumValues.get("sumTotalEmissions"));
        }
	return totalUtility;
  }
  
  public double routeUtilityCalulationP5(JsonTrip tripResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
		
	double totalDuration = (double) this.getTripTotalDuration(tripResult);
    double totalEmissions = this.getTripTotalEmissions(tripResult);
    double nominalEmissions = this.getTripNominalEmissions(tripResult);
    
    System.out.println("trip emissions: " + totalEmissions);
    
	double durationCriterionValue = 0;
	if (totalDuration <= 10){
		durationCriterionValue = (Double)userPreferences.get("duration10min");
	} 
	else if (10 < totalDuration && totalDuration < 20){
		durationCriterionValue = (Double)userPreferences.get("duration30min");
	}
	else{
		durationCriterionValue = (Double)userPreferences.get("duration30plus");
	}
        
    int numberOfChanges = tripResult.getSegments().size();
    double comfortCriterionValue = 0;
    
    if (numberOfChanges <= 2){
        comfortCriterionValue = (Double) userPreferences.get("comfortHigh");
    }
    else if (numberOfChanges > 2 && numberOfChanges <= 3){
        comfortCriterionValue = (Double) userPreferences.get("comfortMedium");
    }
    else {
        comfortCriterionValue = (Double) userPreferences.get("comfortLow");
    }
	
	double wbDuration = (double) this.getTripTotalWBDuration(tripResult);
	double wbDurationCriterionValue = 0;
	if (wbDuration > 0){
		if (wbDuration <= 10){
			wbDurationCriterionValue = (Double)userPreferences.get("WB10min");
		} 
		else if (10 < wbDuration && wbDuration <= 30){
			wbDurationCriterionValue = (Double)userPreferences.get("WB30min");
		}
		else{
			wbDurationCriterionValue = (Double)userPreferences.get("WB30plus");
		}
	}
	
	double emissionsCriterionValue = 0;
	if (totalEmissions <= 1.0*nominalEmissions){
		emissionsCriterionValue = (Double)userPreferences.get("emissionsLow");
	}
	else if (totalEmissions > 1.0*nominalEmissions && totalEmissions <= 1.6*nominalEmissions){
		emissionsCriterionValue = (Double)userPreferences.get("emissionsMedium");
	}
	else{
		emissionsCriterionValue = (Double)userPreferences.get("emissionsHigh");
	}
		
	totalUtility = durationCriterionValue*
						((Double)userPreferences.get("durationImportance")) 
						+ 
						wbDurationCriterionValue*
						((Double)userPreferences.get("wbtimeImportance"))
                                                +
                                                comfortCriterionValue*
                				((Double)userPreferences.get("comfortImportance"))
                                                ;
    
	totalUtility = totalUtility*(1.0-(Double)userPreferences.get("ecoAttitudeImportance"))
								- emissionsCriterionValue*(Double)userPreferences.get("ecoAttitudeImportance");
	return totalUtility;
  }
  
    private void updateTotalWBDurationStats(ArrayList<JsonResponseRoute> routeResults){
      for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
	          double walkingBicycleDuration = getTripTotalWBDuration(trip);
	          if (maxValues.containsKey("maxWBTotalDuration")){
	            double currentMaxTotalDuration = (Double) maxValues.get("maxWBTotalDuration");
	            if (currentMaxTotalDuration < walkingBicycleDuration){
	                maxValues.put("maxWBTotalDuration", walkingBicycleDuration);
	            }
	          }
	          else{
	              maxValues.put("maxWBTotalDuration", walkingBicycleDuration);
	          }
	
	          if (minValues.containsKey("minWBTotalDuration")){
	            double currentMinTotalDuration = (Double) minValues.get("minWBTotalDuration");
	            if (currentMinTotalDuration > walkingBicycleDuration){
	                minValues.put("minWBTotalDuration", walkingBicycleDuration);
	            }
	          }
	          else{
	              minValues.put("minWBTotalDuration", walkingBicycleDuration);
	          }
	
	          if (sumValues.containsKey("sumWBTotalDuration")){
	            double sumTotalDuration = (Double) sumValues.get("sumWBTotalDuration");
	            sumTotalDuration += walkingBicycleDuration;
	            sumValues.put("sumWBTotalDuration", sumTotalDuration);
	          }
	          else{
	              sumValues.put("sumWBTotalDuration", walkingBicycleDuration);
	          } 
    	  }
      }      
  }
  
  private void updateTotalDurationStats(ArrayList<JsonResponseRoute> routeResults){      
      for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
	        double totalDuration = getTripTotalDuration(trip);
	        if (maxValues.containsKey("maxTotalDuration")){
	            double currentMaxTotalDuration = (Double) maxValues.get("maxTotalDuration");
	            if (currentMaxTotalDuration < totalDuration){
	                maxValues.put("maxTotalDuration", totalDuration);
	            }
	        }
	        else{
	            maxValues.put("maxTotalDuration", totalDuration);
	        }
	
	        if (minValues.containsKey("minTotalDuration")){
	          double currentMinTotalDuration = (Double) minValues.get("minTotalDuration");
	          if (currentMinTotalDuration > totalDuration){
	              minValues.put("minTotalDuration", totalDuration);
	          }
	        }
	        else{
	            minValues.put("minTotalDuration", totalDuration);
	        }
	
	        if (sumValues.containsKey("sumTotalDuration")){
	          double sumTotalDuration = (Double) sumValues.get("sumTotalDuration");
	          sumTotalDuration += totalDuration;
	          sumValues.put("sumTotalDuration", sumTotalDuration);
	        }
	        else{
	            sumValues.put("sumTotalDuration", totalDuration);
	        }
    	  }
      }           
  }
  
  private void updateTotalEmissionStats(ArrayList<JsonResponseRoute> routeResults){      
      for(JsonResponseRoute route : routeResults){
    	  for(JsonTrip trip : route.getTrips()){
	        double totalEmissions = getTripTotalEmissions(trip);
	        if (maxValues.containsKey("maxTotalEmissions")){
	            double currentMaxTotalEmissions = (Double) maxValues.get("maxTotalEmissions");
	            if (currentMaxTotalEmissions < totalEmissions){
	                maxValues.put("maxTotalEmissions", totalEmissions);
	            }
	        }
	        else{
	            maxValues.put("maxTotalEmissions", totalEmissions);
	        }
	
	        if (minValues.containsKey("minTotalEmissions")){
	          double currentMinTotalEmissions = (Double) minValues.get("minTotalEmissions");
	          if (currentMinTotalEmissions > totalEmissions){
	              minValues.put("minTotalEmissions", totalEmissions);
	          }
	        }
	        else{
	            minValues.put("minTotalEmissions", totalEmissions);
	        }
	
	        if (sumValues.containsKey("sumTotalEmissions")){
	          double sumTotalEmissions = (Double) sumValues.get("sumTotalEmissions");
	          sumTotalEmissions += totalEmissions;
	          sumValues.put("sumTotalEmissions", sumTotalEmissions);
	        }
	        else{
	            sumValues.put("sumTotalEmissions", totalEmissions);
	        }
    	  }
      }
  }
  
  //per trip total duration
  private double getTripTotalDuration(JsonTrip trip){
      double result = 0.0;
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
          result += (double)segment.getDurationMinutes();
          j++;
      }
      return result;
  }
  
  //per trip total emissions
  private double getTripTotalEmissions(JsonTrip trip){
      double result = 0.0;
      
      HashMap emissions = new HashMap<String, Double>();
      emissions.put("ptMetro", 20.0);
      emissions.put("ptBusCity", 25.5);
      emissions.put("ptTram", 96.6);
      emissions.put("car", 169.0);
      emissions.put("walk", 0.0);
      emissions.put("bike", 0.0);
      
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
    	  if (!(segment.getVehicle() == null) && !(segment.getVehicle().getType() == null)){
	    	  result += ((Double)emissions.get(segment.getVehicle().getType()))
	    			  *(segment.getDistanceMeter()/1000.0);
    	  }
          j++;
      }
      return result;
  }
  
  //per trip total emissions
  private double getTripNominalEmissions(JsonTrip trip){
      double result = 0.0;
      
      double nominalEmissions = 25.5; // this is the bus emissions
      
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
    	  if (!(segment.getVehicle() == null) && !(segment.getVehicle().getType() == null)){
	    	  result += nominalEmissions
	    			  *(segment.getDistanceMeter()/1000.0);
    	  }
          j++;
      }
      return result;
  }
  
  
  
  //per trip total walking or bicycle duration
  private double getTripTotalWBDuration(JsonTrip trip){
      double result = 0.0;
      List<JsonSegment> segments = trip.getSegments();
      int j = 0;
      while (j < segments.size()) {
    	  JsonSegment segment = segments.get(j);
          if (trip.getModality().matches("walk") || trip.getModality().matches("bike")){
            result += (double)segment.getDurationMinutes();
          }
          j++;
      }
      return result;
  }
  
  static LinkedHashMap sortByValue(LinkedHashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
                  return ((Comparable) ((Map.Entry) (o2)).getValue())
                 .compareTo(((Map.Entry) (o1)).getValue());
             }
        });

       LinkedHashMap result = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
           Map.Entry entry = (Map.Entry)it.next();
           result.put(entry.getKey(), entry.getValue());
       }
       return result;
   }
  
}