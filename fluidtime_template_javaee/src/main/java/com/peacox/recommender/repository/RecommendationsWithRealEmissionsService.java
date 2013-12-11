package com.peacox.recommender.repository;

import java.util.List;

public interface RecommendationsWithRealEmissionsService {
	
	public RecommendationsWithRealEmissions findRecommendationsById(int id);
	public RecommendationsWithRealEmissions findRecommendationsByUserIdTimestamp(long user_id);
	public RecommendationsWithRealEmissions create(RecommendationsWithRealEmissions recommendations);
	public RecommendationsWithRealEmissions update(RecommendationsWithRealEmissions recommendations);
	public List<RecommendationsWithRealEmissions> getAll();
	
}
