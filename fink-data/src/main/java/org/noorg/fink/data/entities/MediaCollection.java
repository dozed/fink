package org.noorg.fink.data.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@NodeEntity
public class MediaCollection implements Media {
	
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

	@RelatedTo(elementClass = Image.class, type="PART_OF")
	private Set<Image> items = new HashSet<Image>();
	
	private Image cover;
	
	public MediaCollection() {}
	
	public MediaCollection(String title) {
		setDate(new DateTime());
		setUuid(UUID.randomUUID().toString());
		setTitle(title);
	}

	public void addItem(Image item) {
		items.add(item);
	}
	
	public List<Image> getItems() {
		return ImmutableList.copyOf(items);
	}

	public void setCover(Image cover) {
		this.cover = cover;
	}
	
	public Image getCover() {
		return cover;
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
