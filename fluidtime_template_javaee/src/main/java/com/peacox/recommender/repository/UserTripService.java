package com.peacox.recommender.repository;

import java.util.List;

public interface UserTripService {
	
	public UserTrip findUserTripByUserId(Long id);
	public UserTrip create(UserTrip userTrip);
	public List<UserTrip> findAllUserTrip();
	public List<UserTrip> getUserTripsForRequestId(int requestId);
	public UserTrip getUserTripsForRequestIdAndOrderId(long requestId, int orderId);
}
