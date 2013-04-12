package com.peacox.recommender.repository;

import java.util.List;

public interface UserRouteRequestService {
	
	public UserRouteRequest findRouteRequestById(int id);
	public UserRouteRequest findRouteRequestByUserIdTimestamp(long user_id);
	public UserRouteRequest create(UserRouteRequest routeRequest);
	
}
