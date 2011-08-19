package org.noorg.fink.data.entities;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.google.common.collect.ImmutableSet;

@NodeEntity
public class Post {
	
	/**
	 * workaround for inheritance bug:
	 * - properties can be in super-type, but needs to be @NodeEntity
	 * - relations cant be in type when super-type is @NodeEntity
	 * 
	 * so i moved all properties to the concrete type.
	 */
	@Indexed private Long id;
	@Indexed private String uuid;
	@Indexed private DateTime date;
	@Indexed String title;
	private String author;
	@RelatedTo(elementClass = Tag.class, type="TAGGED_WITH") private Set<Tag> tags = new TreeSet<Tag>();

	private String text;
	private Category category;

	Post() {}

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

	/**
	 * workaround getters/setters
	 */
	public Long getId() {
		return id;
	}

	public DateTime getDate() {
		return date;
	}

	public Set<Tag> getTags() {
		return ImmutableSet.copyOf(tags);
	}

	public String getUuid() {
		return uuid;
	}

	protected void setDate(DateTime date) {
		this.date = date;
	}

	public void addTag(Tag tag) {
		this.tags.add(tag);
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
