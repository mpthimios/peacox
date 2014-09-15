package com.peacox.recommender.repository;

import java.util.Date;
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
	
	public List<Recommendations> getAll(){
		TypedQuery query = em.createQuery("select r from Recommendations r where r.timestamp > '2013-06-19 00:00:00.000'", Recommendations.class);	    
	    List<Recommendations> result = null;
	    try{
	    	result = (List<Recommendations>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 		
	}
	
	public List<Recommendations> getAllByDate(Date date){
		TypedQuery query = em.createQuery("select r from Recommendations r where r.timestamp > ?1", Recommendations.class);
		query.setParameter(1, date);
	    List<Recommendations> result = null;
	    try{
	    	result = (List<Recommendations>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 		
	}
	
	public Recommendations update(Recommendations recommendations) {
		// TODO Auto-generated method stub
		repository.update(recommendations.getId(), recommendations.getUser_id(), recommendations.getSessionId());
		
		return recommendations;
	}

	public List<Recommendations> getAllByDateRange(Date startDate, Date endDate) {
		TypedQuery query = em.createQuery("select r from Recommendations r where r.timestamp > ?1 and r.timestamp < ?2", Recommendations.class);
		query.setParameter(1, startDate);
		query.setParameter(2, endDate);
	    List<Recommendations> result = null;
	    try{
	    	result = (List<Recommendations>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 
	}
}
