package com.peacox.recommender.repository;

import java.util.Date;
import java.util.List;



public interface StagesService {
	
	public List<Stages> findStagesByUserId(Long id);
	public List<Stages> findStagesByUserIdAndDate(Long id, Date start, Date end);
	public List<Stages> findStagesByUserIdAndHour(Long id, int startHour, int endHour);
	public int findNumberOfDaysTraced(Long userId);
}
