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

public class RouteRequest{
//get user preferences
	
	private String fromStr = "";
	private String fromWGS84Lon = "";
	private String fromWGS84Lat = "";
	
	private String toStr = "";
	private String toWGS84Lon = "";
	private String toWGS84Lat = "";
	
	private String coordinatesType = "";
	
	private String userId = "";
	
	public ArrayList getRouteRequestOptions(){
		
		LinkedHashMap routeRequestOptions = new LinkedHashMap();
		
		routeRequestOptions.put("fromStr", fromStr);
		routeRequestOptions.put("fromWGS84Lon", fromWGS84Lon);
		routeRequestOptions.put("fromWGS84Lat", fromWGS84Lat);
		routeRequestOptions.put("toStr", toStr);
		routeRequestOptions.put("toWGS84Lon", toWGS84Lon);
		routeRequestOptions.put("toWGS84Lat", toWGS84Lat);
		routeRequestOptions.put("userId", userId);
		routeRequestOptions.put("coordinatesType", coordinatesType);
		
        return null;
    }

	public String getFromStr() {
		return fromStr;
	}

	public void setFromStr(String fromStr) {
		this.fromStr = fromStr;
	}

	public String getFromWGS84Lon() {
		return fromWGS84Lon;
	}

	public void setFromWGS84Lon(String fromWGS84Lon) {
		this.fromWGS84Lon = fromWGS84Lon;
	}

	public String getFromWGS84Lat() {
		return fromWGS84Lat;
	}

	public void setFromWGS84Lat(String fromWGS84Lat) {
		this.fromWGS84Lat = fromWGS84Lat;
	}

	public String getToStr() {
		return toStr;
	}

	public void setToStr(String toStr) {
		this.toStr = toStr;
	}

	public String getToWGS84Lon() {
		return toWGS84Lon;
	}

	public void setToWGS84Lon(String toWGS84Lon) {
		this.toWGS84Lon = toWGS84Lon;
	}

	public String getToWGS84Lat() {
		return toWGS84Lat;
	}

	public void setToWGS84Lat(String toWGS84Lat) {
		this.toWGS84Lat = toWGS84Lat;
	}

	public String getCoordinatesType() {
		return coordinatesType;
	}

	public void setCoordinatesType(String coordinatesType) {
		this.coordinatesType = coordinatesType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	
    
}