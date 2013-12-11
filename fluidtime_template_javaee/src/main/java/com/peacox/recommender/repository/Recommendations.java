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
@Table(schema="public", name = "recommendations")

public class Recommendations extends AbstractRecommendation {

	@Override
	public String toString() {
		return "user [id=" + id + ", recommendation=" + recommendation + "]";
	}
}
