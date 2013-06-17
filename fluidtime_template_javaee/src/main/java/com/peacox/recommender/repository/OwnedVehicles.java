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
@Table(name = "`user_vehicle`")

public class OwnedVehicles {

	protected Long user_id;
	protected boolean has_catalyst;
	protected int emission_cat;
	protected double engine_size;
	protected double vehicle_weight;
	String feul_type;

	public OwnedVehicles() {
		super();
	}


	@Id
	@Column(name = "`user_id`")
	public Long getUserId() {
		return user_id;
	}

	public void setUserId(Long user_id) {
		this.user_id = user_id;
	}

	@Column(name = "has_catalyst")
	public boolean getHasCatalyst() {
		return has_catalyst;
	}

	public void setHasCatalyst(boolean has_catalyst) {
		this.has_catalyst = has_catalyst;
	}
	
	@Column(name = "emission_cat")
	public int getEmissionCat() {
		return emission_cat;
	}

	public void setEmissionCat(int emission_cat) {
		this.emission_cat = emission_cat;
	}
	
	@Column(name = "engine_size")
	public double getEngineSize() {
		return engine_size;
	}

	public void setEngineSize(double engine_size) {
		this.engine_size = engine_size;
	}
	
	@Column(name = "vehicle_weight")
	public double getVehicleWeight() {
		return vehicle_weight;
	}

	public void setVehicleWeight(double vehicle_weight) {
		this.vehicle_weight = vehicle_weight;
	}
	
	@Column(name = "feul_type")
	public String getFeulType() {
		return feul_type;
	}

	public void setFeulType(String feul_type) {
		this.feul_type = feul_type;
	}
	
	@Override
	public String toString() {
		return "ownedVehicles [id=" + user_id + "]";
	}
}
