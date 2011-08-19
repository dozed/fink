package org.noorg.fink.data.entities;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.joda.time.DateTime;
import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ContentItem {

	@Indexed private Long id;
	@Indexed private String uuid;
	private Set<Tag> tags = new TreeSet<Tag>();
	@Indexed private DateTime date;
	@Indexed(fieldName="title", indexName="title_index") String title;
	private String author;

	ContentItem() { }
	
	public ContentItem(Node n) {
		setPersistentState(n);
	}

	public ContentItem(String title, String author) {
		this.uuid = UUID.randomUUID().toString(); 
		this.date = new DateTime();
		this.title = title;
		this.author = author;
	}

	public Long getId() {
		return id;
	}

	public DateTime getDate() {
		return date;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public String getUuid() {
		return uuid;
	}

	protected void setDate(DateTime date) {
		this.date = date;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	protected void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
}
