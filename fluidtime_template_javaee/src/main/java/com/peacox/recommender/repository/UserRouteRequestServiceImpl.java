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
public class UserRouteRequestServiceImpl implements UserRouteRequestService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserRouteRequestRepository repository;

	public UserRouteRequest findRouteRequestById(int id) {
		TypedQuery query = em.createQuery("select r from UserRouteRequest r where r.id = ?1", UserRouteRequest.class);
	    query.setParameter(1, id);
	 
	    return (UserRouteRequest)query.getSingleResult();		
	}

	public UserRouteRequest findRouteRequestByUserIdTimestamp(long user_id) {
		TypedQuery query = em.createQuery("select r from UserRouteRequest r where r.user_id = ?1 order by timestamp DESC limit 1", UserRouteRequest.class);
	    query.setParameter(1, user_id);
	    UserRouteRequest result = null;
	    try{
	    	result = (UserRouteRequest)query.getResultList().get(0);
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 		
	}

	public UserRouteRequest create(UserRouteRequest routeRequest) {
		repository.save(routeRequest);
		return routeRequest;
	}
}
