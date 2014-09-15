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
	protected double nbr_viewed_car;
	protected double nbr_viewed_pt;
	protected double nbr_viewed_bike;
	protected double nbr_viewed_walk;
	protected double nbr_selected_car;
	protected double nbr_selected_pt;
	protected double nbr_selected_walk;
	protected double nbr_selected_bike;
	
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
	
	public double getNbr_viewed_car() {
		return nbr_viewed_car;
	}

	public void setNbr_viewed_car(double nbr_viewed_car) {
		this.nbr_viewed_car = nbr_viewed_car;
	}

	public double getNbr_viewed_pt() {
		return nbr_viewed_pt;
	}

	public void setNbr_viewed_pt(double nbr_viewed_pt) {
		this.nbr_viewed_pt = nbr_viewed_pt;
	}

	public double getNbr_viewed_bike() {
		return nbr_viewed_bike;
	}

	public void setNbr_viewed_bike(double nbr_viewed_bike) {
		this.nbr_viewed_bike = nbr_viewed_bike;
	}

	public double getNbr_viewed_walk() {
		return nbr_viewed_walk;
	}

	public void setNbr_viewed_walk(double nbr_viewed_walk) {
		this.nbr_viewed_walk = nbr_viewed_walk;
	}

	public double getNbr_selected_car() {
		return nbr_selected_car;
	}

	public void setNbr_selected_car(double nbr_selected_car) {
		this.nbr_selected_car = nbr_selected_car;
	}

	public double getNbr_selected_pt() {
		return nbr_selected_pt;
	}

	public void setNbr_selected_pt(double nbr_selected_pt) {
		this.nbr_selected_pt = nbr_selected_pt;
	}

	public double getNbr_selected_walk() {
		return nbr_selected_walk;
	}

	public void setNbr_selected_walk(double nbr_selected_walk) {
		this.nbr_selected_walk = nbr_selected_walk;
	}

	public double getNbr_selected_bike() {
		return nbr_selected_bike;
	}

	public void setNbr_selected_bike(double nbr_selected_bike) {
		this.nbr_selected_bike = nbr_selected_bike;
	}

	@Override
	public String toString() {
		return "user [id=" + user_id + ", message=" + user_id + "]";
	}
}
