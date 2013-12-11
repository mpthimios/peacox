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
public class RecommendationsWithRealEmissionsServiceImpl implements RecommendationsWithRealEmissionsService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private RecommendationsWithRealEmissionsRepository repository;

	public RecommendationsWithRealEmissions findRecommendationsById(int id) {
		TypedQuery query = em.createQuery("select r from RecommendationsWithRealEmissions r where r.id = ?1", RecommendationsWithRealEmissions.class);
	    query.setParameter(1, id);
	 
	    return (RecommendationsWithRealEmissions)query.getSingleResult();		
	}

	public RecommendationsWithRealEmissions findRecommendationsByUserIdTimestamp(long user_id) {
		TypedQuery query = em.createQuery("select r from RecommendationsWithRealEmissions r where r.user_id = ?1 order by timestamp DESC limit 1", RecommendationsWithRealEmissions.class);
	    query.setParameter(1, user_id);
	 
	    return (RecommendationsWithRealEmissions)query.getResultList().get(0);		
	}

	public RecommendationsWithRealEmissions create(RecommendationsWithRealEmissions recommendations) {
		repository.save(recommendations);
		return recommendations;
	}
	
	public List<RecommendationsWithRealEmissions> getAll(){
		TypedQuery query = em.createQuery("select r from RecommendationsWithRealEmissions r where r.timestamp > '2013-06-19 00:00:00.000'", RecommendationsWithRealEmissions.class);	    
	    List<RecommendationsWithRealEmissions> result = null;
	    try{
	    	result = (List<RecommendationsWithRealEmissions>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 		
	}
	
	public RecommendationsWithRealEmissions update(RecommendationsWithRealEmissions recommendations) {
		// TODO Auto-generated method stub
		repository.update(recommendations.getId(), recommendations.getUser_id(), recommendations.getSessionId());
		
		return recommendations;
	}
}
