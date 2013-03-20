package com.peacox.recommender;

import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.fluidtime.brivel.route.json.response.JsonResponseRoute;
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
  
  public LinkedHashMap getRecommendations(LinkedHashMap userPreferences, ArrayList routeResults){
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
          ArrayList routeResults){
        
        Combination combination = new Combination(routeResults.size(), 3);
        //ArrayList<String> myArr = new ArrayList<String>();
        LinkedHashMap combinationsAndUtilities = new LinkedHashMap<ArrayList, Double>();
        HashMap routesAndUtilities = new HashMap<RouteDto, Double>();
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
                                RouteDto routeResult = (RouteDto) routeResults.get(pos);
                                double routeUtility = 0.0;
                                switch(((Double)userPreferences.get("utilityAlgorithm")).intValue()){
                                    case 0: routeUtility += routeUtilityCalulation(routeResult, userPreferences);                                        
                                        break;
                                    case 1: routeUtility += routeUtilityCalulation(routeResult, userPreferences);                                        
                                        break;
                                    case 2: routeUtility += routeUtilityCalulationP2(routeResult, userPreferences);
                                        break;
                                    case 3: routeUtility += routeUtilityCalulationP3(routeResult, userPreferences);
                                        break;
                                    case 4: routeUtility += routeUtilityCalulationP4(routeResult, userPreferences);
                                        break;
                                    case 5: routeUtility += routeUtilityCalulationP5(routeResult, userPreferences);
                                    break;
                                    default: routeUtility += routeUtilityCalulation(routeResult, userPreferences);
                                        break;                                        
                                }
                                utility += routeUtility;
                                if (!routesAndUtilities.containsKey(routeResult)){
                                    routesAndUtilities.put(routeResult, routeUtility);
                                }
                                //utility += routeUtilityCalulation(routeResult, userPreferences);
                                totalDuration += (double)getRouteTotalDuration(routeResult);
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
        
        LinkedHashMap finalRouteResults = new LinkedHashMap<Integer, HashMap>();

        ArrayList finalRoutes = null;
        System.out.println("before iterating routes");
        int itCounter = 0;
        Iterator it=combinationsAndUtilities.keySet().iterator();
        while ( it.hasNext()) {
                if (itCounter == index){
                        finalRoutes = (ArrayList)it.next();
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
        int routeCounter = 0;
        for (int temp = 0; temp < finalRoutes.size(); temp++){
                RouteDto route = (RouteDto) (routeResults.get((Integer)finalRoutes.get(temp)));
                HashMap routeWithUtility = new HashMap<RouteDto, Double>();
                routeWithUtility.put(route, routesAndUtilities.get(route));
                finalRouteResults.put(routeCounter,routeWithUtility);
                topListValues.add((Integer)finalRoutes.get(temp));
                routeCounter++;
        }
        
        //Iterate routeResults and add the remainding results to the final routes
        for (int temp = 0; temp < routeResults.size(); temp++){
            if (!topListValues.contains(temp)){
                RouteDto route = (RouteDto) ((RouteDto) (routeResults.get(temp)));
                HashMap routeWithUtility = new HashMap<RouteDto, Double>();
                routeWithUtility.put(route, routesAndUtilities.get(route));
                finalRouteResults.put(routeCounter,routeWithUtility);               
                routeCounter++;
            }
        }
        System.out.println("after finding the route");
             
        //Print results to check if we have UTF-8
        Collection c = finalRouteResults.values();
        Iterator itr = c.iterator();
        PrintWriter outUTF8 = new PrintWriter(new OutputStreamWriter(System.out));
        int ci = 1;
//	while (itr.hasNext()){
//            RouteDto routeResult = (RouteResult)itr.next();
//            if (routeResult != null) {
//                outUTF8.println("Duration: " + routeResult.getTotalDuration());			  
//		outUTF8.println("Type of Result " + routeResult.getTypeOfResult());
//		Collection c1 = routeResult.getPartialRouteList().values();
//		Iterator itr1 = c1.iterator();
//		while (itr1.hasNext()){
//                    RouteSegment routeSegment = (RouteSegment)itr1.next();
//                    outUTF8.println("Segment Duration: " + routeSegment.getTimeMinute());
//                    outUTF8.println("Segment type: " + routeSegment.getType());                    
//                    outUTF8.println("Segment Distance: " + routeSegment.getDistance());
//                    outUTF8.println("Segment Means of Transport: " + routeSegment.getMeansOfTransport());
//                    outUTF8.println("Streets:");
//                    LinkedHashMap pathDescription = routeSegment.getPathDescription();
//                    int j = 0;
//                    for(Object key : pathDescription.keySet()){
//                        SegmentDescriptionElem segmentDescription = (SegmentDescriptionElem) pathDescription.get(key);
//                        outUTF8.println("street: " + segmentDescription.getStreetname());
//                    }
//		}
//            }
//            //i++;
//	}
        
       // System.out.println("combinationsAndUtilities size: ");
      
      return finalRouteResults;
  }
   
  private LinkedHashMap methodForRecommendations2(LinkedHashMap userPreferences, 
          ArrayList routeResults){
        
        //ArrayList<String> myArr = new ArrayList<String>();
        LinkedHashMap resultsAndUtilities = new LinkedHashMap<JsonResponseRoute, Double>();
        
        Iterator iterator = routeResults.iterator();
        while ( iterator.hasNext() ){
            RouteDto routeResult = ( RouteDto ) iterator.next();                        
        
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
                    case 0: utility += routeUtilityCalulation(routeResult, userPreferences);
                        break;
                    case 1: utility += routeUtilityCalulationP1(routeResult, userPreferences);
                        break;
                    case 2: utility += routeUtilityCalulationP2(routeResult, userPreferences);
                        break;
                    case 3: utility += routeUtilityCalulationP3(routeResult, userPreferences);
                        break;
                    case 4: utility += routeUtilityCalulationP4(routeResult, userPreferences);
                        break;                                        
                    default: utility += routeUtilityCalulation(routeResult, userPreferences);
                        break;                                        
                }
                //utility += routeUtilityCalulation(routeResult, userPreferences);
                totalDuration += (double)this.getRouteTotalDuration(routeResult);
                    
                resultsAndUtilities.put(routeResult, utility);
                howMany++;
            }
            catch(Exception e){
                    e.printStackTrace();	
            }
        }	
	System.out.println("number of routes fetched: " + resultsAndUtilities.size());

        LinkedHashMap finalRouteResults = new LinkedHashMap<Integer, JsonResponseRoute>();

        LinkedHashMap tmpFinalRouteResults = null; //GetRoutes.sortByValue(resultsAndUtilities);
        Iterator iterator1 = tmpFinalRouteResults.keySet().iterator();
        int position = 0;
        
        System.out.println("++++++ Going to Print Ordered Utilities: ++++++");
        System.out.println(" resultsAndUtilities size: " + resultsAndUtilities.size());
        System.out.println("tmpFinalRouteResults size: " + tmpFinalRouteResults.size());
        while ( iterator1.hasNext() ){
        	JsonResponseRoute key = ( JsonResponseRoute ) iterator1.next();
            finalRouteResults.put(position, key);
            System.out.println("position: " + position + " utility: " + (Double)tmpFinalRouteResults.get(key)
                    + " totalDuration: " + "key.getTotalDuration()" 
                    + " wbDuration: " + "key.getWBDuration()");
            position++;
        }
        
      return finalRouteResults;
  }
  
   public double routeUtilityCalulation(RouteDto routeResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getRouteTotalDuration(routeResult);
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
	
	double wbDuration = (double) getRouteTotalWBDuration(routeResult);
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

  public double routeUtilityCalulationP1(RouteDto routeResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getRouteTotalDuration(routeResult);
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
	
	double wbDuration = (double) getRouteTotalWBDuration(routeResult);
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
  
  public double routeUtilityCalulationP2(RouteDto routeResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getRouteTotalDuration(routeResult);
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
	
	double wbDuration = (double) this.getRouteTotalWBDuration(routeResult);
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
  
  public double routeUtilityCalulationP3(RouteDto routeResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) getRouteTotalDuration(routeResult);
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
	
	double wbDuration = (double) this.getRouteTotalWBDuration(routeResult);
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
  
  public double routeUtilityCalulationP4(RouteDto routeResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) this.getRouteTotalDuration(routeResult);
        double totalEmissions = this.getRouteTotalEmissions(routeResult);
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
        
        int numberOfChanges = routeResult.getTrips().size();
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
	
	double wbDuration = (double) this.getRouteTotalWBDuration(routeResult);
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
        
        if (true){
            totalUtility = totalUtility * totalEmissions/((Double)sumValues.get("sumTotalEmissions"));
        }
	return totalUtility;
  }
  
  public double routeUtilityCalulationP5(RouteDto routeResult, LinkedHashMap<String, Double> userPreferences){
	double totalUtility = 0;
	
	double totalDuration = (double) this.getRouteTotalDuration(routeResult);
        double totalEmissions = this.getRouteTotalEmissions(routeResult);
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
        
        int numberOfChanges = routeResult.getTrips().size();
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
	
	double wbDuration = (double) this.getRouteTotalWBDuration(routeResult);
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
        
        if (true){
            totalUtility = totalUtility * totalEmissions/((Double)sumValues.get("sumTotalEmissions"));
        }
	return totalUtility;
  }
  
    private void updateTotalWBDurationStats(ArrayList<RouteDto> routeResults){
      for(RouteDto route : routeResults){
          double walkingBicycleDuration = getRouteTotalWBDuration(route);
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
  
  private void updateTotalDurationStats(ArrayList<RouteDto> routeResults){
      
      for(RouteDto route : routeResults){
        double totalDuration = getRouteTotalDuration(route);
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
  
  private void updateTotalEmissionStats(ArrayList<RouteDto> routeResults){
      
      for(RouteDto route : routeResults){
        double totalEmissions = getRouteTotalEmissions(route);
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
  
  private double getRouteTotalDuration(RouteDto route){
      double result = 0.0;
      for (TripDto trip : route.getTrips()){
          result += (double)trip.getDurationMinutes();          
      }
      return result;
  }
  
  private double getRouteTotalEmissions(RouteDto route){
      double result = 0.0;
      for (TripDto trip : route.getTrips()){
          result += (double)Double.parseDouble(trip.getDescription()); 
      }
      return result;
  }
  
  private double getRouteTotalWBDuration(RouteDto route){
      double result = 0.0;
      for (TripDto trip : route.getTrips()){
          if (trip.getModality().matches("walk") || trip.getModality().matches("bicycle")){
            result += (double)trip.getDurationMinutes();
          }
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