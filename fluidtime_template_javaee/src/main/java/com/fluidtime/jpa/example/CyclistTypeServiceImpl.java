package com.fluidtime.jpa.example;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Repository
@Service
public class CyclistTypeServiceImpl implements CyclistTypeService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private CyclistTypeRepository repository;

	public List<CyclistType> getCyclistTypeList() {
		return repository.findAll();
	}

	public boolean isValidCyclistTypeId(Long id) {
		if (repository.findOne(id) == null)
			return false;
		return true;
	}

	public CyclistType create(CyclistType cyclistType) {
		repository.save(cyclistType);
		return cyclistType;
	}

	public Long count() {
		return repository.count();
	}
	
	public CyclistType findCyclistTypeByName(String name) {
		return repository.findCyclistTypeByName(name);
	}
}
