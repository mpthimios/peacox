package com.fluidtime.jpa.example;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.*;;


@Transactional(readOnly = true)
public interface CyclistTypeRepository extends JpaRepository<CyclistType, Long> {
	public CyclistType findCyclistTypeByName(String name);
}
