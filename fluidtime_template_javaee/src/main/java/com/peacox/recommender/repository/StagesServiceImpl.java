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
	
	public List<Stages> findStagesByUserIdAndHour(Long id, int startHour, int endHour) {
		TypedQuery query = em.createQuery("select s from Stages s where s.user_id = ?1 and date_part('hour', s.start_date_time) > ?2 and date_part('hour', s.end_date_time) <= ?3", Stages.class);
	    query.setParameter(1, id);
	    query.setParameter(2, startHour);
	    query.setParameter(3, endHour);
	 
	    return query.getResultList();
	}

	public int findNumberOfDaysTraced(Long userId) {
		
		TypedQuery query = em.createQuery("SELECT to_char(start_date_time, 'YYYY MM DD') " + 
				"FROM Stages s WHERE s.user_id = ?1 GROUP by to_char(start_date_time, 'YYYY MM DD') ORDER by to_char(start_date_time, 'YYYY MM DD')", String.class);
	    query.setParameter(1, userId);
		return query.getResultList().size();
	}

	
}
