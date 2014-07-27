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
@Table(schema="public", name = "recommender_messages")

public class RecommenderMessages {

	protected int id;		
	protected int strategy;
	protected int context;
	protected int transportation_means;
	protected int user_type;
	protected String text;
	protected String text_de;
	
	
	public RecommenderMessages() {
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

	
	public int getStrategy() {
		return strategy;
	}


	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}


	public int getContext() {
		return context;
	}


	public void setContext(int context) {
		this.context = context;
	}


	public int getTransportation_means() {
		return transportation_means;
	}


	public void setTransportation_means(int transportation_means) {
		this.transportation_means = transportation_means;
	}


	public int getUser_type() {
		return user_type;
	}


	public void setUser_type(int user_type) {
		this.user_type = user_type;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}
		
	public String getText_de() {
		return text_de;
	}


	public void setText_de(String text_de) {
		this.text_de = text_de;
	}


	@Override
	public String toString() {
		return "user [id=" + id + ", message=" + id + "]";
	}
}
