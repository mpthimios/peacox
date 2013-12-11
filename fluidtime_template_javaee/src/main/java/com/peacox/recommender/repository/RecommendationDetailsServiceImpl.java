package com.peacox.recommender.repository;

import java.util.Calendar;
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
public class RecommendationDetailsServiceImpl implements RecommendationDetailsService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private RecommendationDetailsRepository repository;

	public List<RecommendationDetails> getAll(){
		TypedQuery query = em.createQuery("select r from RecommendationDetails r where r.timestamp > '2013-06-19 00:00:00.000'", RecommendationDetails.class);	    
	    List<RecommendationDetails> result = null;
	    try{
	    	result = (List<RecommendationDetails>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 		
	}
	
	public RecommendationDetails getFirstBeforeDate(Date date, int user_id){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, 1);
		Date startDate = cal.getTime();
		TypedQuery query = em.createQuery("select r from RecommendationDetails r where user_id = ?1 order by abs(" +
			   " (DATE_PART('day', timestamp - ?2) * 24 + " + 
               " DATE_PART('hour', timestamp - ?2)) * 60 + " +
               " DATE_PART('minute', timestamp - ?2)) ASC", RecommendationDetails.class);	    
		//query.setParameter(1, date);
		query.setParameter(1, Long.parseLong(Integer.toString(user_id)));
		query.setParameter(2, startDate);
		query.setMaxResults(1);
		List<RecommendationDetails> result = null;
	    try{
	    	result = (List<RecommendationDetails>)query.getResultList();
	    	if (result.size() > 0){
	    		return result.iterator().next();
	    	}
	    	else{
	    		return null;
	    	}
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    	return null;
	    }	    		
	}
}
