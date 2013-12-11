package com.peacox.recommender.userdiaries;

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
import com.peacox.recommender.repository.RecommendationDetails;
import com.peacox.recommender.repository.Stages;
import com.peacox.recommender.repository.StagesService;
import com.peacox.recommender.repository.User;
import com.peacox.recommender.repository.UserTreeScores;
import com.peacox.recommender.repository.UserTreeScoresService;
import com.peacox.recommender.webservice.Webservice;

@Component("UserDiary")
public class UserDiary{

    private String personCode = "";
    private int userId;
    private int tripNrOverall;
    private int tripNrPerPerson;
    private Date startDateTime;
    private Date endDateTime;
    private String transportMode = "";
    private String tripPurpose = "";
    private int usedRoutePlanner;
	private String comment = "";
	private double distance = 0.0;
	private double duration = 0.0;
	private double avgSpeed = 0.0;
	
	private RecommendationDetails recommendationDetails = null;
	public String getPersonCode() {
		return personCode;
	}
	public void setPersonCode(String personCode) {
		this.personCode = personCode;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getTripNrOverall() {
		return tripNrOverall;
	}
	public void setTripNrOverall(int tripNrOverall) {
		this.tripNrOverall = tripNrOverall;
	}
	public int getTripNrPerPerson() {
		return tripNrPerPerson;
	}
	public void setTripNrPerPerson(int tripNrPerPerson) {
		this.tripNrPerPerson = tripNrPerPerson;
	}
	public Date getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	public Date getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	public String getTransportMode() {
		return transportMode;
	}
	public void setTransportMode(String transportMode) {
		this.transportMode = transportMode;
	}
	public String getTripPurpose() {
		return tripPurpose;
	}
	public void setTripPurpose(String tripPurpose) {
		this.tripPurpose = tripPurpose;
	}
	public int getUsedRoutePlanner() {
		return usedRoutePlanner;
	}
	public void setUsedRoutePlanner(int usedRoutePlanner) {
		this.usedRoutePlanner = usedRoutePlanner;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public RecommendationDetails getRecommendationDetails() {
		return recommendationDetails;
	}
	public void setRecommendationDetails(RecommendationDetails recommendationDetails) {
		this.recommendationDetails = recommendationDetails;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		this.duration = duration;
	}
	public double getAvgSpeed() {
		return avgSpeed;
	}
	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
    
}