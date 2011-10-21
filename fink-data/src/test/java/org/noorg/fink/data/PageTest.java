package org.noorg.fink.data;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.noorg.fink.data.entities.Page;
import org.noorg.fink.data.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "/spring/testContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PageTest {

	@Autowired
	private PageRepository pageRepository;

	@Autowired
	private Neo4jTemplate template;

	@Rollback(false)
	@BeforeTransaction
	public void clearDatabase() {
		Neo4jHelper.cleanDb(template);
	}

	@Test
	public void shouldCreatePages() {
		pageRepository.createSomePages();
		assertEquals(3, pageRepository.countPages());
	}

	@Test
	public void shouldPersistPages() {
		pageRepository.createSomePages();
		Page p = pageRepository.findPageByTitle("root");
		assertNotNull(p);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldContainSubPages() {
		pageRepository.createSomePages();
		Page p = pageRepository.findPageByTitle("root");
		assertNotNull(p);
		assertEquals(2, p.getSubPages().size());
		
		Iterator<Page> it = p.getSubPages().iterator();
		
		assertThat(it.next().getTitle(), is(anyOf(containsString("sub1"), containsString("sub2"))));
		assertThat(it.next().getTitle(), is(anyOf(containsString("sub1"), containsString("sub2"))));
	}
	
	@Test
	public void shouldHaveTags() {
		pageRepository.createSomePages();
		Page p = pageRepository.findPageByTitle("root");
		assertEquals(3, p.getTags().size());
	}
	
}
