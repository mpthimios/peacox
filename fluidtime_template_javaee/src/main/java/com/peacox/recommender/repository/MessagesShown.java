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
@Table(schema="public", name = "messages_shown")

public class MessagesShown {

	protected int id;		
	protected Date timestamp;
	protected String oid;
	protected long user_id;
	protected String session_id;
	protected int strategy;
	protected int context;
	protected int message_id;
	protected int transportation_means;
	protected String text;
	
	public MessagesShown() {
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

	
	public Date getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	public String getOid() {
		return oid;
	}


	public void setOid(String oid) {
		this.oid = oid;
	}


	public long getUser_id() {
		return user_id;
	}


	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}


	public String getSession_id() {
		return session_id;
	}


	public void setSession_id(String session_id) {
		this.session_id = session_id;
	}

	//1. Feedback
	//2. Simulation
	//3. Authority
	//4. SelfMonitoring
	//5. Reminders
	//6. Praise
	//7. SocialComparison
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


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}

	public int getTransportation_means() {
		return transportation_means;
	}


	public void setTransportation_means(int transportation_means) {
		this.transportation_means = transportation_means;
	}

	
	public int getMessage_id() {
		return message_id;
	}


	public void setMessage_id(int message_id) {
		this.message_id = message_id;
	}


	@Override
	public String toString() {
		return "user [id=" + id + ", message=" + id + "]";
	}
}
