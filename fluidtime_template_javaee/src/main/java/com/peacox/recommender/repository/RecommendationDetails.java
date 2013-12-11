package com.peacox.recommender.repository;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Table(schema="public", name = "recommendations")
public class RecommendationDetails extends AbstractRecommendation{
	
	private UserRouteResult routeResult;
	private UserRouteRequest routeRequest;
	
	public RecommendationDetails() {
		super();
	}
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "session_id",nullable=true, referencedColumnName = "session_id", insertable = false, updatable = false)
    public UserRouteRequest getUserRouteRequest(){
		return this.routeRequest;
	}
	public void setUserRouteRequest(UserRouteRequest routeRequest){
		this.routeRequest = routeRequest;
	}
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "session_id",nullable=true, referencedColumnName = "session_id", insertable = false, updatable = false)
    public UserRouteResult getUserRouteResult(){
		return this.routeResult;
	}
	public void setUserRouteResult(UserRouteResult routeResult){
		this.routeResult = routeResult;
	}

	@Override
	public String toString() {
		return "user [id=" + id + ", recommendation=" + recommendation + "]";
	}
}
