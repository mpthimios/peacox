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
public class EmissionStatisticsServiceImpl implements EmissionStatisticsService {


	@PersistenceContext
	protected EntityManager em;

	@Autowired
	private EmissionStatisticsRepository repository;

	public EmissionStatistics create(EmissionStatistics emissionStatistics) {
		repository.save(emissionStatistics);
		return emissionStatistics;
	}

}
