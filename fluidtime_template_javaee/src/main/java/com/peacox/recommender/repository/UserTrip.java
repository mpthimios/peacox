package com.peacox.recommender.repository;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.NamedQuery;

@Entity
@Table(schema="public", name = "user_trip")

public class UserTrip {

	protected Long id;
	protected int user_id;
	protected long route_id;
	protected int trip_id;
	protected boolean is_viewed;
	protected boolean is_selected;	
	protected Date date_time; 

	public UserTrip() {
		super();
	}


	@Id
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getUser_id() {
		return user_id;
	}


	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}


	public long getRoute_id() {
		return route_id;
	}


	public void setRoute_id(long route_id) {
		this.route_id = route_id;
	}


	public int getTrip_id() {
		return trip_id;
	}


	public void setTrip_id(int trip_id) {
		this.trip_id = trip_id;
	}


	public boolean getIs_viewed() {
		return is_viewed;
	}


	public void setIs_viewed(boolean is_viewed) {
		this.is_viewed = is_viewed;
	}


	public boolean getIs_selected() {
		return is_selected;
	}


	public void setIs_selected(boolean is_selected) {
		this.is_selected = is_selected;
	}


	public Date getDate_time() {
		return date_time;
	}


	public void setDate_time(Date date_time) {
		this.date_time = date_time;
	}


	@Override
	public String toString() {
		return "user [id=" + id + "]";
	}
}
