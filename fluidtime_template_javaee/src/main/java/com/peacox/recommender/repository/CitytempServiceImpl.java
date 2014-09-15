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
public class CitytempServiceImpl implements CitytempService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private CitytempRepository repository;

	public Citytemp findCitytempById(Long id) {
		TypedQuery query = em.createQuery("select c from Citytemp c where c.id = ?1", Citytemp.class);
	    query.setParameter(1, id);
	 
	    return (Citytemp)query.getSingleResult();
	}

	public Citytemp create(Citytemp citytemp) {
		repository.save(citytemp);
		return citytemp;
	}

	public List<Citytemp> findCitytempByDate(Date date) {
		TypedQuery query = em.createQuery("select c from Citytemp c where c.time > ?1 order by time ASC", Citytemp.class);	    
	    query.setParameter(1, date);	   	 
	    return query.getResultList();
	}

	public List<Citytemp> findCitytempByDateAndCity(Date date, String city) {
		TypedQuery query = em.createQuery("select c from Citytemp c where c.city = ?2 c.time > ?1 order by time ASC", Citytemp.class);	    
	    query.setParameter(1, date);	   	 
	    query.setParameter(2, city);	   	 
	    return query.getResultList();
	}

	
}
