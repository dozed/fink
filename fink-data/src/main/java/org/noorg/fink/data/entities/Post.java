package org.noorg.fink.data.entities;

import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Post extends ContentItem {
	
	private String text;
	private Category category;

	public Post() {}

	public Post(String title, String text, String author, Category category, Set<Tag> tags) {
		setUuid(UUID.randomUUID().toString());
		setDate(new DateTime());
		setTitle(title);
		setAuthor(author);
		this.text = text;
		this.category = category;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

}
