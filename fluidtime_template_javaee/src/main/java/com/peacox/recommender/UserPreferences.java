package com.peacox.recommender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.util.JRubyFile;

import com.fluidtime.routeExample.model.RouteDto;
import com.fluidtime.routeExample.model.TripDto;
import com.peacox.recommender.utils.SimulatedRoutes;

public class UserPreferences{
//get user preferences
	
	protected String EXTENDED = "ON";
	
	private double duration10min = 10.0;
	private double duration30min = 4.0;
	private double duration30plus = 1.0;
	
	private double WB10min = 10.0;
	private double WB30min = 4.0;
	private double WB30plus = 1.0;	
	
	private double comfortHigh = 4.0;
	private double comfortMedium = 10.0;
	private double comfortLow = 1.0;
	
	private double durationImportance = 50.0/100.0;
	private double wbtimeImportance = 10.0/100.0;
	private double comfortImportance = 20.0/100.0;
	private double ecoAttitudeImportance = 20.0/100;
	
	private double emissionsLow = 1.0;
	private double emissionsMedium = 3.0;
	private double emissionsHigh = 6.0;
	
	
	
	private double orderAlgorithm = 1.0;
	private double utilityAlgorithm = 5.0;
		
    public LinkedHashMap getUserPreferences(){

        LinkedHashMap userPreferences = new LinkedHashMap<String, Double>();
        userPreferences.put("duration10min", duration10min);
        userPreferences.put("duration30min", duration30min);
        userPreferences.put("duration30plus", duration30plus);
        userPreferences.put("WB10min", WB10min);
        userPreferences.put("WB30min", WB30min);
        userPreferences.put("WB30plus", WB30plus);
        userPreferences.put("comfortHigh", comfortHigh);
        userPreferences.put("comfortMedium", comfortMedium);
        userPreferences.put("comfortLow", comfortLow);
        userPreferences.put("durationImportance", durationImportance);
        userPreferences.put("wbtimeImportance", wbtimeImportance);
        userPreferences.put("comfortImportance", comfortImportance);
        userPreferences.put("orderAlgorithm", orderAlgorithm);
        userPreferences.put("utilityAlgorithm", utilityAlgorithm);
        
        if (EXTENDED.matches("ON")){
        	//get jruby engine
    	    ScriptEngine jruby = new ScriptEngineManager().getEngineByName("jruby");
    	    ArrayList<RouteDto> routes = null;
    	    //process a ruby file    
    	    try {	    	
    			jruby.eval(new BufferedReader(new InputStreamReader(UserPreferences.class.
    			    	getClassLoader().
    			    	getResourceAsStream("fetch_user.rb"))));
    			
    			jruby.put("userid","53");    		    
    			RubyHash result = (RubyHash) jruby.eval("get_preferences($userid)");
    		    System.out.println("ruby result in preferences carAddict: " + result.get("carAddict"));
    		    userPreferences.put("carPreference", (Double)result.get("carAddict"));
    		    userPreferences.put("ptPreference", (Double)result.get("ptAddict"));
    		    userPreferences.put("walkPreference", (Double)result.get("walkAddict"));
    		    userPreferences.put("bikePreference", (Double)result.get("bikeAddict"));
    		    userPreferences.put("emissionsLow", 1.0);
    		    userPreferences.put("emissionsMedium", 3.0);
    		    userPreferences.put("emissionsHigh", 6.0);
    		    userPreferences.put("ecoAttitudeImportance", 0.2);
    		    
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			System.out.println("something really bad happened in preferences");
    			e.printStackTrace();
    		}
        }
        
        return userPreferences;
    }
    
    public void setSenario(int scenarioId){

        double[][] scenariosArray = {
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 60.0, 20.0, 20.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 70.0, 10.0, 20.0},
        		{1.0, 4.0, 10.0, 1.0, 4.0, 10.0, 4.0, 10.0, 1.0, 40.0, 0.0, 60.0},
        		{10.0, 1.0, 4.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 60.0, 20.0, 20.0},
        		{4.0, 10.0, 1.0, 1.0, 4.0, 10.0, 10.0, 4.0, 1.0, 20.0, 40.0, 40.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 60.0, 10.0, 30.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 60.0, 10.0, 30.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 60.0, 10.0, 30.0},
        		{1.0, 4.0, 10.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 60.0, 10.0, 30.0},
        		{4.0, 10.0, 1.0, 10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 60.0, 20.0, 20.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 70.0, 0.0, 30.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 60.0, 10.0, 30.0},
        		{1.0, 4.0, 10.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 50.0, 20.0, 30.0},
        		{1.0, 4.0, 10.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 50.0, 0.0, 50.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 60.0, 20.0, 20.0},
        		{10.0, 4.0, 1.0, 10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 100.0, 0.0, 0.0},
        		{1.0, 4.0, 10.0, 4.0, 10.0, 1.0, 4.0, 10.0, 1.0, 40.0, 40.0, 20.0},
        		{10.0, 4.0, 1.0, 4.0, 10.0, 1.0, 4.0, 10.0, 1.0, 60.0, 20.0, 20.0}
        };
        
        this.setDuration10min(scenariosArray[scenarioId][0]);
        this.setDuration30min(scenariosArray[scenarioId][1]);
        this.setDuration30plus(scenariosArray[scenarioId][2]);
        this.setWB10min(scenariosArray[scenarioId][3]);
        this.setWB30min(scenariosArray[scenarioId][4]);
        this.setWB30plus(scenariosArray[scenarioId][5]);
        this.setComfortHigh(scenariosArray[scenarioId][6]);
        this.setComfortMedium(scenariosArray[scenarioId][7]);
        this.setComfortLow(scenariosArray[scenarioId][8]);
        this.setDurationImportance(scenariosArray[scenarioId][9]);
        this.setWbtimeImportance(scenariosArray[scenarioId][10]);
        this.setComfortImportance(scenariosArray[scenarioId][11]);
    }
    
    public String[] getSenarioAddresses(int scenarioId){

        String[][] addressesArray = {
        		{"Webgasse 6",	"Universitatsring 1"},
        		{"Webgasse 6",	"Modecenterstrasse 17"},
        		{"Webgasse 6",	"Universitatsring 1"},
        		{"Operngasse 25",	"Wahringer Strasse 59"},
        		{"Webgasse 6",	"Donauinsel 1"},
        		{"Haslingergasse 14",	"Modecenterstrasse 17"},
        		{"Neumayrgasse 2",	"Kinderspitalgasse 15"},
        		{"Neumayrgasse 2",	"Liebiggasse 5"},
        		{"Neumayrgasse 2",	"Thugutstrasse 4"},
        		{"Karl Benz Weg 91",	"Modecenterstrasse 17"},
        		{"Modecenterstrasse 17",	"Schoppenhauerstrasse 49"},
        		{"Karl Benz Weg 91",	"Wulzendorfstrasse 1"},
        		{"Gentzgasse 52",	"Pramergasse 27"},
        		{"Karl Benz Weg 91",	"Hoechstadtplatz 5"},
        		{"Lindengasse 14",	"Modecenterstrasse 17"},
        		{"Lindengasse 14",	"Vorgartenstrasse 134"},
        		{"Beingasse 17",	"Hauffgasse 4"},
        		{"Beingasse 17",	"Hadikgasse 28"}
        };
        
        return addressesArray[scenarioId];
    }
	
	public double getDuration10min() {
		return duration10min;
	}

	public void setDuration10min(double duration10min) {
		this.duration10min = duration10min;
	}

	public double getDuration30min() {
		return duration30min;
	}

	public void setDuration30min(double duration30min) {
		this.duration30min = duration30min;
	}

	public double getDuration30plus() {
		return duration30plus;
	}

	public void setDuration30plus(double duration30plus) {
		this.duration30plus = duration30plus;
	}

	public double getWB10min() {
		return WB10min;
	}

	public void setWB10min(double wB10min) {
		WB10min = wB10min;
	}

	public double getWB30min() {
		return WB30min;
	}

	public void setWB30min(double wB30min) {
		WB30min = wB30min;
	}

	public double getWB30plus() {
		return WB30plus;
	}

	public void setWB30plus(double wB30plus) {
		WB30plus = wB30plus;
	}

	public double getComfortHigh() {
		return comfortHigh;
	}

	public void setComfortHigh(double comfortHigh) {
		this.comfortHigh = comfortHigh;
	}

	public double getComfortMedium() {
		return comfortMedium;
	}

	public void setComfortMedium(double comfortMedium) {
		this.comfortMedium = comfortMedium;
	}

	public double getComfortLow() {
		return comfortLow;
	}

	public void setComfortLow(double comfortLow) {
		this.comfortLow = comfortLow;
	}

	public double getDurationImportance() {
		return durationImportance;
	}

	public void setDurationImportance(double durationImportance) {
		this.durationImportance = durationImportance;
	}

	public double getWbtimeImportance() {
		return wbtimeImportance;
	}

	public void setWbtimeImportance(double wbtimeImportance) {
		this.wbtimeImportance = wbtimeImportance;
	}

	public double getComfortImportance() {
		return comfortImportance;
	}

	public void setComfortImportance(double comfortImportance) {
		this.comfortImportance = comfortImportance;
	}
	
	public double getEmissionsLow() {
		return emissionsLow;
	}

	public void setEmissionsLow(double emissionsLow) {
		this.emissionsLow = emissionsLow;
	}

	public double getEmissionsMedium() {
		return emissionsMedium;
	}

	public void setEmissionsMedium(double emissionsMedium) {
		this.emissionsMedium = emissionsMedium;
	}

	public double getEmissionsHigh() {
		return emissionsHigh;
	}

	public void setEmissionsHigh(double emissionsHigh) {
		this.emissionsHigh = emissionsHigh;
	}

	public double getEcoAttitudeImportance() {
		return ecoAttitudeImportance;
	}

	public void setEcoAttitudeImportance(double ecoAttitudeImportance) {
		this.ecoAttitudeImportance = ecoAttitudeImportance;
	}

	public double getOrderAlgorithm() {
		return orderAlgorithm;
	}

	public void setOrderAlgorithm(double orderAlgorithm) {
		this.orderAlgorithm = orderAlgorithm;
	}

	public double getUtilityAlgorithm() {
		return utilityAlgorithm;
	}

	public void setUtilityAlgorithm(double utilityAlgorithm) {
		this.utilityAlgorithm = utilityAlgorithm;
	}
	
}