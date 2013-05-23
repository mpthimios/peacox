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
@Table(schema="public", name = "`emissionStatistics`")

public class EmissionStatistics {

	protected int id;	
	protected Timestamp timestamp;
	protected int stage_id;
	protected double emissions_estimation;	
	
	public EmissionStatistics() {
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

	@Column(name="timestamp")
	public Timestamp getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	@Column(name="stage_id")
	public int getStage_id() {
		return stage_id;
	}


	public void setStage_id(int stage_id) {
		this.stage_id = stage_id;
	}

	@Column(name="emissions_estimation")
	public double getEmissions_estimation() {
		return emissions_estimation;
	}


	public void setEmissions_estimation(double emissions_estimation) {
		this.emissions_estimation = emissions_estimation;
	}


	
	@Override
	public String toString() {
		return "user [id=" + id + ", emissions=" + id + "]";
	}
}
