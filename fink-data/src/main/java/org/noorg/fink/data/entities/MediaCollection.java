package org.noorg.fink.data.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.joda.time.DateTime;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@NodeEntity
public class MediaCollection {
	
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
	
	// this entity is accessible via a shortlink
	@Indexed private String shortlink;
	
	private Image cover;
	
	public MediaCollection(Node n) {
		setPersistentState(n);
	}

	public MediaCollection() {}
	
	public MediaCollection(String title) {
		setDate(new DateTime());
		setUuid(UUID.randomUUID().toString());
		setTitle(title);
		setShortlink(title);
	}

		
	public List<Image> getSortedImages() {
		return getItems();
	}
	
	@Transactional
	public void sortImages(String[] uuids) {
		int valid = 0;
		
		// check if all items occur in the incoming array
		for (String uuid : uuids) {
			for (Image i : items) {
				if (i.getUuid().equals(uuid)) {
					valid++;
					break;
				}
			}
		}
		
		List<Image> items = getItems();
		
		if (valid == items.size()) {
			int sorting = 0;
			Transaction tx = getPersistentState().getGraphDatabase().beginTx();
			try {
				for (String uuid : uuids) {
					for (Image img : items) {
						if (img.getUuid().equals(uuid)) {
							Relationship r = getRelationshipTo(img, "PART_OF");
							r.setProperty("sorting", sorting++);
						}
					}
				}
				tx.success();
			} finally {
				tx.finish();
			}
		}
	}

	public void addItem(Image item) {
		items.add(item);
	}
	
	public List<Image> getItems() {
		Iterable<Image> it = findAllByQuery("start collection=(%collection) match (collection)-[p:PART_OF]->(image) return image order by p.sorting?", Image.class, MapUtil.map("collection", this.getNodeId()));
		return ImmutableList.copyOf(it);
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
