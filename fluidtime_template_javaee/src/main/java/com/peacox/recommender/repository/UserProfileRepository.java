package com.peacox.recommender.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.*;;


@Transactional(readOnly = true)
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
	@Modifying  
	@Transactional
	@Query("update UserProfile r set nbr_viewed_car = ?2, nbr_viewed_pt = ?3," +
			" nbr_viewed_bike = ?4, nbr_viewed_walk = ?5, " +
			" nbr_selected_car = ?6, nbr_selected_pt = ?7, nbr_selected_bike = ?8," +
			" nbr_selected_walk = ?9 where user_id = ?1 ")
	void updateNbrViewedCar(long userId, Double nbr_viewed_car, Double nbr_viewed_pt,
			Double nbr_viewed_bike, Double nbr_viewed_walk, Double nbr_selected_car, 
			Double nbr_selected_pt, Double nbr_selected_bike, Double nbr_selected_walk);
		
}
