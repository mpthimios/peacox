package com.peacox.recommender.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.*;;


@Transactional(readOnly = false)
public interface UserTreeScoresRepository extends JpaRepository<UserTreeScores, Long> {
	@Modifying  
	@Transactional
	@Query("update UserTreeScores u set u.score = ?2 where u.user_id = ?1 ")
	void update(long userId, double score);
}
