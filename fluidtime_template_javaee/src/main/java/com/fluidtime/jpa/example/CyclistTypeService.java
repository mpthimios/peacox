package com.fluidtime.jpa.example;

import java.util.List;

public interface CyclistTypeService {
	
	public List<CyclistType> getCyclistTypeList();
	public boolean isValidCyclistTypeId(Long id);
	public CyclistType create(CyclistType cyclistType);
	public Long count();
	public CyclistType findCyclistTypeByName(String name);
}
