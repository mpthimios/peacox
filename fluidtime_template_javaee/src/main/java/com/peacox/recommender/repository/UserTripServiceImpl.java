package com.peacox.recommender.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@Service
public class UserTripServiceImpl implements UserTripService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserTripRepository repository;

	public UserTrip findRouteResultById(int id) {
		
	    return null;		
	}

	public UserRouteResult findUserTripByUserIdTimestamp(long user_id) {
		
	    return null;		
	}

	public UserTrip create(UserTrip userTrip) {
		repository.save(userTrip);
		return userTrip;
	}
	
	public UserTrip update(UserTrip userTrip) {
		// TODO Auto-generated method stub
		//repository.update(routeResult.getId(), routeResult.getSessionId());
		return null;
	}
	
	public List<UserTrip> getAll(){
		
	    return null; 		
	}
	
	public List<UserTrip> getUserTripsForRequestId(int requestId){
		TypedQuery query = em.createQuery("select r from UserTrip r where r.route_id = ?1", UserTrip.class);
		query.setParameter(1, requestId);
	    List<UserTrip> result = null;
	    try{
	    	result = (List<UserTrip>)query.getResultList();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    return result; 	
	}

	public UserTrip findUserTripByUserId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<UserTrip> findAllUserTrip() {
		// TODO Auto-generated method stub
		return null;
	}

	public UserTrip getUserTripsForRequestIdAndOrderId(long requestId,
			int orderId) {
		TypedQuery query = em.createQuery("select r from UserTrip r where r.route_id = ?1 and r.trip_id = ?2", UserTrip.class);
		query.setParameter(1, requestId);
		query.setParameter(2, orderId);
	    UserTrip result = null;
	    try{
	    	result = (UserTrip)query.getSingleResult();
	    }catch(IndexOutOfBoundsException e){
	    	//nothing to do for now
	    }
	    catch(NoResultException e){
	    	//nothing to do for now
	    }
	    return result;
	}
}
