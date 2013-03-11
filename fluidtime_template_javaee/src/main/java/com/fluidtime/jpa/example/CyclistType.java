package com.fluidtime.jpa.example;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "cyclist_type")
@SequenceGenerator(name = "cyclist_type_seq_gen", sequenceName = "cyclist_type_id_seq", allocationSize = 1, initialValue = 1)
public class CyclistType {

	protected Long id;
	protected String name;
	protected String title;

	public CyclistType() {
		super();
	}

	public CyclistType(String name) {
		super();
		this.name = name;
	}
	
	public CyclistType(String name, String title) {
		super();
		this.name = name;
		this.title = title;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cyclist_type_seq_gen")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(columnDefinition = "varchar", length = 50, name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Column(columnDefinition = "varchar", length = 50, name = "title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return "CyclistType [id=" + id + ", name=" + name + "]";
	}
}
