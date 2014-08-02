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
@Table(schema="public", name = "user_profile")

public class UserProfile {

	protected long user_id;		
	protected int mobility_behaviour;
	protected double rewards;
	protected double social_comparison;
	protected double authority;
	protected double reciprocity;
	protected double liking;
	protected double commitment;
	
	public UserProfile() {
		super();		
	}
	
	@Id
	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}





	public int getMobility_behaviour() {
		return mobility_behaviour;
	}





	public void setMobility_behaviour(int mobility_behaviour) {
		this.mobility_behaviour = mobility_behaviour;
	}





	public double getRewards() {
		return rewards;
	}





	public void setRewards(double rewards) {
		this.rewards = rewards;
	}





	public double getSocial_comparison() {
		return social_comparison;
	}





	public void setSocial_comparison(double social_comparison) {
		this.social_comparison = social_comparison;
	}





	public double getAuthority() {
		return authority;
	}





	public void setAuthority(double authority) {
		this.authority = authority;
	}





	public double getReciprocity() {
		return reciprocity;
	}





	public void setReciprocity(double reciprocity) {
		this.reciprocity = reciprocity;
	}





	public double getLiking() {
		return liking;
	}





	public void setLiking(double liking) {
		this.liking = liking;
	}





	public double getCommitment() {
		return commitment;
	}





	public void setCommitment(double commitment) {
		this.commitment = commitment;
	}


	@Override
	public String toString() {
		return "user [id=" + user_id + ", message=" + user_id + "]";
	}
}
