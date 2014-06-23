package com.peacox.recommender.repository;

import java.util.ArrayList;
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
public class RecommenderMessagesServiceImpl implements RecommenderMessagesService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private MessagesShownRepository repository;

	public List<RecommenderMessages> getRecommenderMessages() {
		TypedQuery query = em.createQuery("from RecommenderMessages ", RecommenderMessages.class);	    
	    List<RecommenderMessages> result = null;
	    try {
	    	result = (List<RecommenderMessages>)query.getResultList();
	    }
	    catch(NoResultException nre){
	    	Log.error("could not find results in UserTreeScores");
	    }
		return result;
	}

	
}
