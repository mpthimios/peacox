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
public class OwnedVehiclesServiceImpl implements OwnedVehiclesService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private OwnedVehiclesRepository repository;

//	public List<OwnedVehicles> getOwnedVehiclesList() {
//		return repository.findAll();
//	}
//
//	public boolean isValidOwnedVehiclesTypeId(Long id) {
//		if (repository.findOne(id) == null)
//			return false;
//		return true;
//	}
//
	public OwnedVehicles create(OwnedVehicles ownedVehicle) {
		repository.save(ownedVehicle);
		return ownedVehicle;
	}
//
//	public Long count() {
//		return repository.count();
//	}
//	
	public List<OwnedVehicles> findOwnedVehiclesByUserId(Long userId) {
		TypedQuery query = em.createQuery("select a from OwnedVehicles a where a.user_id = ?1", OwnedVehicles.class);
	    query.setParameter(1, userId);
	 
	    return query.getResultList();

	}
}
