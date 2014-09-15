package com.peacox.recommender.repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface CitytempService {
	
	public Citytemp findCitytempById(Long id);
	public Citytemp create(Citytemp citytemp);
	public List<Citytemp> findCitytempByDate(Date date);
	public List<Citytemp> findCitytempByDateAndCity(Date date, String city);
}
