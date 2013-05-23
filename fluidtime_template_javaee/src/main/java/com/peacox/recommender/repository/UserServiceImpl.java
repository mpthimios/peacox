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
public class UserServiceImpl implements UserService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserRepository repository;

	public User create(User user) {
		repository.save(user);
		return user;
	}

	public User findUserByUserId(Long id) {
		TypedQuery query = em.createQuery("select a from User a where a.id = ?1", User.class);
	    query.setParameter(1, id);
	 
	    return (User)query.getSingleResult();

	}

	public List<User> findAllUsers() {
		TypedQuery query = em.createQuery("from User", User.class);	    
	 
	    return query.getResultList();
	}
}
