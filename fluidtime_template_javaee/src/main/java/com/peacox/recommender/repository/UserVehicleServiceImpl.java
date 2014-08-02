package com.peacox.recommender.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import jline.internal.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@Service
public class UserVehicleServiceImpl implements UserVehicleService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserVehicleRepository repository;

	public UserVehicle findUserVehicleByUserId(Long user_id) {
		TypedQuery query = em.createQuery("select a from UserVehicle a where a.user_id = ?1", UserVehicle.class);
	    query.setParameter(1, user_id);
	    try{
	    	return (UserVehicle)query.getSingleResult();
	    }
	    catch(NoResultException nre){
	    	return null;
	    }
	}

		
}
