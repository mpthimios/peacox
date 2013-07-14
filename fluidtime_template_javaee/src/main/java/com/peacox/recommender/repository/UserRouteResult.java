package com.peacox.recommender.repository;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.NamedQuery;

@Entity
@Table(schema="public", name = "route_results")

public class UserRouteResult {

	protected int id;
	protected Date timestamp;
	protected long user_id;
	protected String result;
	protected String session_id;
	
	public UserRouteResult() {
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
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}


	@Column(name="user_id")
	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}


	@Column(name="result", columnDefinition="LONGTEXT")
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Column(name="session_id")
	public String getSessionId() {
		return session_id;
	}

	public void setSessionId(String request) {
		this.session_id = session_id;
	}

	@Override
	public String toString() {
		return "user [id=" + id + ", result=" + result + "]";
	}
}
