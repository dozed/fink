package org.noorg.fink.data.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.joda.time.DateTime;
import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.core.Direction;

import com.google.common.collect.ImmutableSet;

@NodeEntity
@JsonIgnoreProperties({"parentPage", "persistentState", "entityState"})
public class Page {

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

	@RelatedTo(elementClass = Page.class, type="IS_SUBPAGE")
	private Page parentPage;

	@RelatedTo(elementClass = Page.class, type="IS_SUBPAGE", direction=Direction.INCOMING)
	private Set<Page> subPages = new HashSet<Page>();
	
	private String text;
	
	public Page(Node n) {
		setPersistentState(n);
	}
	
	Page() {}

	public Page(String title) {
		this(title, null);
	}

	public Page(String title, String author) {
		this.uuid = UUID.randomUUID().toString(); 
		this.date = new DateTime();
		this.title = title;
		this.author = author;
	}
	
	public Set<Page> getSubPages() {
		return ImmutableSet.copyOf(subPages);
	}
	
	public Page getParentPage() {
		return parentPage;
	}
	
	public void addPage(Page page) {
		if (!subPages.contains(page)) {
			subPages.add(page);
			page.setParentPage(this);
		}
	}
	
	protected void setParentPage(Page parentPage) {
		this.parentPage = parentPage;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public void clearTags() {
		this.tags.clear();
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
