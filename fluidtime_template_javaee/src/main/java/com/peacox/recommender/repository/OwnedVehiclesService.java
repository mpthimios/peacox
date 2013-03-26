package com.peacox.recommender.repository;

import java.util.List;

public interface OwnedVehiclesService {
	
	public List<OwnedVehicles> findOwnedVehiclesByUserId(Long userId);
	public OwnedVehicles create(OwnedVehicles ownedVehicle);
}
