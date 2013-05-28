package com.peacox.recommender.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface UserTreeScoresService {
	
	public UserTreeScores findUserTreeScoresById(Long id);
	public UserTreeScores create(UserTreeScores userTreeScore);
	public UserTreeScores update(UserTreeScores userTreeScore);
	public UserTreeScores findUserTreeScore(Long userId);
}
