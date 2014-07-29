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
public class UserProfileServiceImpl implements UserProfileService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private UserProfileRepository repository;

	public UserProfile create(UserProfile userProfile) {
		repository.save(userProfile);
		return userProfile;
	}

	public UserProfile findUserProfileByUserId(Long id) {
		TypedQuery query = em.createQuery("select a from UserProfile a where a.user_id = ?1", UserProfile.class);
	    query.setParameter(1, id);
	    try{
	    	return (UserProfile)query.getSingleResult();
	    }
	    catch(NoResultException nre){
	    	return null;
	    }
	}

	public List<UserProfile> findAllUserProfiles() {
		TypedQuery query = em.createQuery("from UserProfile", UserProfile.class);	    
	 
	    return query.getResultList();
	}
}
