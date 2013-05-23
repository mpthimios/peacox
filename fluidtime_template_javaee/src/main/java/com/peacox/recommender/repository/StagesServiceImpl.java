package com.peacox.recommender.repository;

import java.util.ArrayList;
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
public class StagesServiceImpl implements StagesService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private StagesRepository repository;

	public List<Stages> findStagesByUserId(Long id) {
		TypedQuery query = em.createQuery("select s from Stages s where s.user_id = ?1", Stages.class);
	    query.setParameter(1, id);
	 
	    return query.getResultList();
	}

	public List<Stages> findStagesByUserIdAndDate(Long id, Date start, Date end) {
		TypedQuery query = em.createQuery("select s from Stages s where s.user_id = ?1 and s.start_date_time > ?2 and s.end_date_time < ?3", Stages.class);
	    query.setParameter(1, id);
	    query.setParameter(2, start);
	    query.setParameter(3, end);
	 
	    return query.getResultList();
	}

	
}
