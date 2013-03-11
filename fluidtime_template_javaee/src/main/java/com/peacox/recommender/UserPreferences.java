package com.peacox.recommender;

import java.util.LinkedHashMap;

public class UserPreferences{
//get user preferences
	
	private double duration10min = 10.0;
	private double duration30min = 4.0;
	private double duration30plus = 1.0;
	
	private double WB10min = 10.0;
	private double WB30min = 4.0;
	private double WB30plus = 1.0;	
	
	private double comfortHigh = 10.0;
	private double comfortMedium = 4.0;
	private double comfortLow = 1.0;
	
	private double durationImportance = 70.0/100.0;
	private double wbtimeImportance = 20.0/100.0;
	private double comfortImportance = 10.0/100.0;
	
	private double orderAlgorithm = 1.0;
	private double utilityAlgorithm = 4.0;
		
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
        
        return userPreferences;
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