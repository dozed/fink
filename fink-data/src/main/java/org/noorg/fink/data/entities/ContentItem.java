package org.noorg.fink.data.entities;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.Indexed.Level;

public abstract class ContentItem {

	@GraphId @Indexed private Long id;
	@Indexed(level=Level.INSTANCE) private String uuid;
	@Indexed(level=Level.INSTANCE) private DateTime date;
	@Indexed(level=Level.INSTANCE) private String title;

	private String author;

	@RelatedTo(elementClass = Tag.class, type="TAGGED_WITH")
	private Set<Tag> tags = new TreeSet<Tag>();

	ContentItem() { }
	
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
	
	public void addTag(Tag tag) {
		tags.add(tag);
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
