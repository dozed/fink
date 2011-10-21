package org.noorg.fink.data.entities;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;

@NodeEntity
@JsonIgnoreProperties({"parentPage", "persistentState", "entityState"})
public class Page extends ContentItem {

	@RelatedTo(elementClass = Page.class, type="IS_SUBPAGE")
	private Page parentPage;

	@RelatedTo(elementClass = Page.class, type="IS_SUBPAGE", direction=Direction.INCOMING)
	private Set<Page> subPages = new HashSet<Page>();
	
	private String text;
	@Indexed private String shortlink;
	
	Page() {}

	public Page(String title) {
		this(title, title.toLowerCase(), null);
	}

	public Page(String title, String shortlink, String author) {
		super(title, author);
		this.shortlink = shortlink;
	}
	
	public Set<Page> getSubPages() {
		// TODO addPage doesnt work with the following line
		//return ImmutableSet.copyOf(subPages);
		return subPages;
	}
	
	public Page getParentPage() {
		return parentPage;
	}
	
	@Transactional
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

	public String getShortlink() {
		return shortlink;
	}

	public void setShortlink(String shortlink) {
		this.shortlink = shortlink;
	}
	
}
