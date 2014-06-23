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
public class MessagesShownServiceImpl implements MessagesShownService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private MessagesShownRepository repository;

	public List<MessagesShown> findMessagesShownByUserId(Long id) {
		TypedQuery query = em.createQuery("select s from MessagesShown s where s.user_id = ?1", MessagesShown.class);
	    query.setParameter(1, id);
	 
	    return query.getResultList();
	}

	public MessagesShown create(MessagesShown messagesShown) {
		repository.save(messagesShown);
		return messagesShown;
	}
}
