package org.noorg.fink.data;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(locations = "/spring/testContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TransactionTest {

	@Autowired
	private PageRepository pageRepository;

	@Autowired
	private Neo4jTemplate template;

	@Test
	@Rollback(false)
	public void shouldCreatePages() {
		Neo4jHelper.cleanDb(template);
		pageRepository.createSomePages();
		assertEquals(3, pageRepository.countPages());
	}
	
	@Test
	@Rollback(false)
	public void shouldAllowAddOutsideOfTransaction() {
		Page p = pageRepository.findPageByTitle("root");
		p.addPage(new Page("test"));
		pageRepository.save(p);
		assertEquals(3, p.getSubPages().size());
		assertNotNull(p);
	}
	
	@Test
	@Rollback(false)
	public void shouldPersistChanges() {
		Page p = pageRepository.findPageByTitle("root");
		assertNotNull(p);
		assertEquals(3, p.getSubPages().size());
	}
}
