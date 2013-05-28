package com.peacox.recommender.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.*;;


@Transactional(readOnly = true)
public interface CitytempRepository extends JpaRepository<Citytemp, Long> {
	//public OwnedVehicles findOwnedVehiclesByUserId(int userId);
}
