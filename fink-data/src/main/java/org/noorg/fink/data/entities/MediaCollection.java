package org.noorg.fink.data.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

@NodeEntity
public class MediaCollection extends ContentItem {

	@RelatedTo(elementClass = Image.class, type = "PART_OF")
	private Set<Image> items = new HashSet<Image>();

	@Indexed
	private String shortlink;

	private Image cover;

	public MediaCollection() {
	}

	public MediaCollection(String title) {
		setDate(new DateTime());
		setUuid(UUID.randomUUID().toString());
		setTitle(title);
		setShortlink(title);
	}

	public Set<Image> getSortedImages() {
		return getItems();
	}

	@Transactional
	public void addItem(Image item) {
		items.add(item);
	}

	public Set<Image> getItems() {
		return items;
	}

	public void setCover(Image cover) {
		this.cover = cover;
	}

	public Image getCover() {
		return cover;
	}

	public String getShortlink() {
		return shortlink;
	}

	public void setShortlink(String shortlink) {
		this.shortlink = shortlink;
	}

}
