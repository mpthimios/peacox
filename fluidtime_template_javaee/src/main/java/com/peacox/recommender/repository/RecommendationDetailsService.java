package com.peacox.recommender.repository;

import java.util.Date;
import java.util.List;

public interface RecommendationDetailsService {
	
	public List<RecommendationDetails> getAll();
	public RecommendationDetails getFirstBeforeDate(Date date, int user_id);
	
}
