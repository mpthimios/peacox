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
public class UserRouteResultServiceImpl implements UserRouteResultService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserRouteResultRepository repository;

	public UserRouteResult findRouteResultById(int id) {
		TypedQuery query = em.createQuery("select r from RouteResult r where r.id = ?1", UserRouteResult.class);
	    query.setParameter(1, id);
	 
	    return (UserRouteResult)query.getSingleResult();		
	}

	public UserRouteResult findRouteResultByUserIdTimestamp(long user_id) {
		TypedQuery query = em.createQuery("select r from RouteResult r where r.user_id = ?1 order by timestamp DESC", UserRouteResult.class);
	    query.setParameter(1, user_id);
	 
	    return (UserRouteResult)query.getSingleResult();		
	}

	public UserRouteResult create(UserRouteResult routeResult) {
		repository.save(routeResult);
		return routeResult;
	}
	
	public List<UserRouteResult> getAll(){
		TypedQuery query = em.createQuery("select r from UserRouteResult r where r.timestamp > '2013-06-19 00:00:00.000'", UserRouteResult.class);	    
	    List<UserRouteResult> result = null;
	    try{
	    	result = (List<UserRouteResult>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 		
	}
}
