package com.peacox.recommender.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.*;;


@Transactional(readOnly = true)
public interface UserTripRepository extends JpaRepository<UserTrip, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
}
