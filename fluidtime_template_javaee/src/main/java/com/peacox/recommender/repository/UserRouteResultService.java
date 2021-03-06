package com.peacox.recommender.repository;

import java.util.List;

public interface UserRouteResultService {
	
	public UserRouteResult findRouteResultById(int id);
	public UserRouteResult findRouteResultByUserIdTimestamp(long user_id);
	public UserRouteResult create(UserRouteResult routeResult);
	public UserRouteResult update(UserRouteResult routeResult);
	public List<UserRouteResult> getAll();
	public List<UserRouteResult> getAllWithNullSessionId();
	
}
