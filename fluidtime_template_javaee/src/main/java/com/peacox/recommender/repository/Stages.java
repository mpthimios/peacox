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
@Table(schema="public", name = "stages")

public class Stages {

	protected int id;	
	protected long user_id;
	protected Timestamp start_date_time;
	protected Timestamp end_date_time;
	protected int mode_detected_code;
	protected int mode_corrected_code;
	protected double duration;
	protected double mean_speed;
	protected double distance;
	
	public Stages() {
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


	@Column(name="user_id")
	public long getUser_id() {
		return user_id;
	}


	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	@Column(name="start_date_time")
	public Timestamp getStart_date_time() {
		return start_date_time;
	}


	public void setStart_date_time(Timestamp start_date_time) {
		this.start_date_time = start_date_time;
	}

	@Column(name="end_date_time")
	public Timestamp getEnd_date_time() {
		return end_date_time;
	}


	public void setEnd_date_time(Timestamp end_date_time) {
		this.end_date_time = end_date_time;
	}

	@Column(name="mode_detected_code")
	public int getMode_detected_code() {
		return mode_detected_code;
	}


	public void setMode_detected_code(int mode_detected_code) {
		this.mode_detected_code = mode_detected_code;
	}

	@Column(name="mode_corrected_code")
	public int getMode_corrected_code() {
		return mode_corrected_code;
	}


	public void setMode_corrected_code(int mode_corrected_code) {
		this.mode_corrected_code = mode_corrected_code;
	}

	@Column(name="duration")
	public double getDuration() {
		return duration;
	}


	public void setDuration(double duration) {
		this.duration = duration;
	}

	@Column(name="mean_speed")
	public double getMean_speed() {
		return mean_speed;
	}


	public void setMean_speed(double mean_speed) {
		this.mean_speed = mean_speed;
	}
	
	@Column(name="distance")
	public double getDistance() {
		return distance;
	}


	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "user [id=" + id + ", stage=" + id + "]";
	}
}
