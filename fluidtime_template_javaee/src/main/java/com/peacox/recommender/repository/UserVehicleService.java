package com.peacox.recommender.repository;

import java.util.List;

public interface UserVehicleService {
	
	public UserVehicle findUserVehicleByUserId(Long user_id);
	//public UserVehicle create(UserVehicle userVehicle);
	//public List<UserVehicle> findAllUserVehicles();		
}
