package org.noorg.fink.data.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.noorg.fink.data.entities.Page;
import org.noorg.fink.data.entities.Tag;
import org.noorg.fink.data.repositories.internal.PageRepositoryInternal;
import org.noorg.fink.data.repositories.internal.TagRepositoryInternal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

@Repository
@Transactional
public class PageRepository {

	@Autowired
	private PageRepositoryInternal repository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private Neo4jTemplate template;

	@Transactional
	public void createSomePages() {
		Page p = new Page("root");
		p.addPage(new Page("sub1"));
		p.addPage(new Page("sub2"));
		p.addTag(new Tag("tagged"));
		p.addTag(new Tag("by"));
		p.addTag(new Tag("artist"));
		repository.save(p);
	}

	@Transactional
	public void save(Page p) {
		repository.save(p);
	}

	public long countPages() {
		return repository.count();
	}

	public Page findPageByUuid(String uuid) {
		return repository.findByPropertyValue("uuid", uuid);
	}

	public Page findPageByTitle(String title) {
		return repository.findByPropertyValue("title", title);
	}

	public Page findPageByShortlink(String shortlink) {
		return repository.findByPropertyValue("shortlink", shortlink);
	}
	
	// TODO do traversal here
	public List<Page> findPagesByTag(String tag) {
		List<Page> pages = new ArrayList<Page>();
		for (Page p : repository.findAll()) {
			for (Tag t : p.getTags()) {
				if (t.getName().equalsIgnoreCase(tag)) {
					pages.add(p);
				}
			}
		}
		return ImmutableList.copyOf(pages);
	}

	public Page findPageByTitleManually(String title) {
		Index<Node> index = template.getIndex(Page.class, "index_title");
		IndexHits<Node> hits = index.get("title", title);
		if (hits.hasNext()) {
			return template.createEntityFromState(hits.next(), Page.class);
		}
		return null;
	}

	public Page find(String property, String value) {
		return repository.findByPropertyValue(property, value);
	}

	public Page findById(long id) {
		return repository.findOne(id);
	}

	public List<Page> findAll() {
		return ImmutableList.copyOf(repository.findAll());
	}

	@Transactional
	public void addSubPage(Page page, Page sub) {
		page.addPage(sub);
	}

	@Transactional
	public Page createPage(String title, String shortlink, String author, Page parent) {
		Page page = new Page(title, shortlink, author);
		parent.addPage(page);
		repository.save(page);
		return page;
	}

	@Transactional
	public void updatePage(String uuid, String parentUuid, String title, String shortlink, String author, String text, String[] tags) {
		Page page = this.findPageByUuid(uuid);
		Page parent = this.findPageByUuid(parentUuid);
		Page oldParent = page.getParentPage();

		if (oldParent != null && oldParent != parent) {
			parent.addPage(page);
			repository.save(oldParent);
		}

		page.clearTags();

		for (String t : tags) {
			Tag tag = tagRepository.findTag(t);
			if (tag == null) {
				tag = tagRepository.createTag(t);
			}
			page.addTag(tag);
		}

		page.setTitle(title);
		page.setShortlink(shortlink);
		page.setAuthor(author);
		page.setText(text);

		repository.save(page);
		repository.save(parent);
	}

	
}
