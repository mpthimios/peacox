package com.peacox.recommender.repository;

import java.util.Date;
import java.util.List;

public interface RecommendationsService {
	
	public Recommendations findRecommendationsById(int id);
	public Recommendations findRecommendationsByUserIdTimestamp(long user_id);
	public Recommendations create(Recommendations recommendations);
	public Recommendations update(Recommendations recommendations);
	public List<Recommendations> getAll();
	public List<Recommendations> getAllByDate(Date date);
	public List<Recommendations> getAllByDateRange(Date startDate, Date endDate);
	
}
