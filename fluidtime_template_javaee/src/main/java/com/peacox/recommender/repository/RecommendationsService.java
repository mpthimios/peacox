package com.peacox.recommender.repository;

import java.util.List;

public interface RecommendationsService {
	
	public Recommendations findRecommendationsById(int id);
	public Recommendations findRecommendationsByUserIdTimestamp(long user_id);
	public Recommendations create(Recommendations recommendations);
	public Recommendations update(Recommendations recommendations);
	public List<Recommendations> getAll();
	
}
