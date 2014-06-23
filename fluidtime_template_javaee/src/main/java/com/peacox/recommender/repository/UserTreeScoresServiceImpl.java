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
public class UserTreeScoresServiceImpl implements UserTreeScoresService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserTreeScoresRepository repository;

	public UserTreeScores findUserTreeScoresById(Long id) {
		TypedQuery query = em.createQuery("select u from UserTreeScores u where u.id = ?1", UserTreeScores.class);
	    query.setParameter(1, id);
	 
	    return (UserTreeScores)query.getSingleResult();
	}

	public UserTreeScores create(UserTreeScores userTreeScore) {
		repository.save(userTreeScore);
		return userTreeScore;
	}

	public UserTreeScores update(UserTreeScores userTreeScore) {
		// TODO Auto-generated method stub
		repository.update(userTreeScore.getUser_id(), userTreeScore.getScore());
		
		return userTreeScore;
	}

	public UserTreeScores findUserTreeScore(Long userId) {
		TypedQuery query = em.createQuery("select u from UserTreeScores u where u.user_id = ?1", UserTreeScores.class);
	    query.setParameter(1, userId);
	    UserTreeScores result = null;
	    try {
	    	result = (UserTreeScores)query.getSingleResult();
	    }
	    catch(NoResultException nre){
	    	//probably this is the first time so we create a new entry
	    	UserTreeScores userTreeScore = new UserTreeScores();
			userTreeScore.setUser_id(userId);
			userTreeScore.setScore(30.0);
			userTreeScore.setLast_update(new Date());
			this.create(userTreeScore);
			result = userTreeScore;
	    }
	    return result;
	}

	public List<UserTreeScores> findAllUserTreeScores() {
		TypedQuery query = em.createQuery("from UserTreeScores ", UserTreeScores.class);	    
	    List<UserTreeScores> result = null;
	    try {
	    	result = (List<UserTreeScores>)query.getResultList();
	    }
	    catch(NoResultException nre){
	    	Log.error("could not find results in UserTreeScores");
	    }
		return result;
	}

	
}
