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
@Table(schema="public", name = "user")

public class User {

	protected Long id;
	protected String first_name;
	protected String last_name;
	protected Date birth_date;
	protected boolean has_disabilities;
	protected boolean eco_altitude;
	protected int eco_score;
	protected String avatar_name;

	public User() {
		super();
	}


	@Id	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	@Column(name = "first_name")
	public String getFirst_name() {
		return first_name;
	}


	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	@Column(name = "last_name")
	public String getLast_name() {
		return last_name;
	}


	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	@Column(name = "birth_date")
	public Date getBirth_date() {
		return birth_date;
	}


	public void setBirth_date(Date birth_date) {
		this.birth_date = birth_date;
	}

	@Column(name = "has_disabilities")
	public boolean isHas_disabilities() {
		return has_disabilities;
	}

	
	public void setHas_disabilities(boolean has_disabilities) {
		this.has_disabilities = has_disabilities;
	}

	@Column(name = "eco_altitude")
	public boolean isEco_altitude() {
		return eco_altitude;
	}


	public void setEco_altitude(boolean eco_altitude) {
		this.eco_altitude = eco_altitude;
	}

	@Column(name = "eco_score")
	public int getEco_score() {
		return eco_score;
	}


	public void setEco_score(int eco_score) {
		this.eco_score = eco_score;
	}

	@Column(name = "avatar_name")
	public String getAvatar_name() {
		return avatar_name;
	}


	public void setAvatar_name(String avatar_name) {
		this.avatar_name = avatar_name;
	}

	@Override
	public String toString() {
		return "user [id=" + id + ", name=" + first_name + last_name + "]";
	}
}
