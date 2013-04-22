package com.peacox.recommender.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@Service
public class RecommendationsServiceImpl implements RecommendationsService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private RecommendationsRepository repository;

	public Recommendations findRecommendationsById(int id) {
		TypedQuery query = em.createQuery("select r from Recommendations r where r.id = ?1", Recommendations.class);
	    query.setParameter(1, id);
	 
	    return (Recommendations)query.getSingleResult();		
	}

	public Recommendations findRecommendationsByUserIdTimestamp(long user_id) {
		TypedQuery query = em.createQuery("select r from Recommendations r where r.user_id = ?1 order by timestamp DESC limit 1", Recommendations.class);
	    query.setParameter(1, user_id);
	 
	    return (Recommendations)query.getResultList().get(0);		
	}

	public Recommendations create(Recommendations recommendations) {
		repository.save(recommendations);
		return recommendations;
	}
}
