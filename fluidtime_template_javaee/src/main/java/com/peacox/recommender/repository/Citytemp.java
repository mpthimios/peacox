package com.peacox.recommender.repository;

import java.sql.Timestamp;
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
@Table(schema="public", name = "iccs_citytemp")

public class Citytemp {

	protected int id;		
	protected Date time;
	protected double temp;
	protected String city;
	protected double precipitation;
	
	public Citytemp() {
		super();		
	}


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	@Column(name="time")
	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}

	@Column(name="temp")
	public double getTemp() {
		return temp;
	}


	public void setTemp(double temp) {
		this.temp = temp;
	}

	@Column(name="precipitation")
	public double getPrecipitation() {
		return precipitation;
	}


	public void setPrecipitation(double precipitation) {
		this.precipitation = precipitation;
	}
	
	@Column(name="city")
	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "user [id=" + id + ", stage=" + id + "]";
	}
}
