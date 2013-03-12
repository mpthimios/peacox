package com.peacox.recommender.repository;

import java.util.List;

public interface OwnedVehiclesService {
	
	public List<OwnedVehicles> findOwnedVehiclesByUserId(int userId);
	public OwnedVehicles create(OwnedVehicles ownedVehicle);
}
