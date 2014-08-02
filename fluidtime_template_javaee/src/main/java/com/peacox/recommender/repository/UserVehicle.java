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
@Table(schema="public", name = "user_vehicle")

public class UserVehicle {

	protected long user_id;
	protected boolean has_catalyst;
	protected int emission_cat;
	protected double engine_size;
	protected double vehicle_weight;
	protected String feul_type;		

	public UserVehicle() {
		super();
	}


	@Id
	public long getUser_id() {
		return user_id;
	}


	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	

	public boolean isHas_catalyst() {
		return has_catalyst;
	}


	public void setHas_catalyst(boolean has_catalyst) {
		this.has_catalyst = has_catalyst;
	}


	public int getEmission_cat() {
		return emission_cat;
	}


	public void setEmission_cat(int emission_cat) {
		this.emission_cat = emission_cat;
	}


	public double getEngine_size() {
		return engine_size;
	}


	public void setEngine_size(double engine_size) {
		this.engine_size = engine_size;
	}


	public double getVehicle_weight() {
		return vehicle_weight;
	}


	public void setVehicle_weight(double vehicle_weight) {
		this.vehicle_weight = vehicle_weight;
	}


	public String getFeul_type() {
		return feul_type;
	}


	public void setFeul_type(String feul_type) {
		this.feul_type = feul_type;
	}


	@Override
	public String toString() {
		return "user [id=" + user_id + "]";
	}
}
