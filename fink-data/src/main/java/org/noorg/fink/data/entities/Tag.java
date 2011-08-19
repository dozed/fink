package org.noorg.fink.data.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
@JsonIgnoreProperties({"persistentState", "entityState"})
public class Tag implements Comparable<Tag> {
	
	private String name;

	public Tag() {}
	
	public Tag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Tag t) {
		return name.compareTo(t.getName());
	}

	@Override
	public String toString() {
		return name;
	}

}
