//package org.noorg.fink.data;
//
//import static junit.framework.Assert.assertEquals;
//
//import java.util.Map;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.neo4j.helpers.collection.MapUtil;
//import org.noorg.fink.data.entities.Image;
//import org.noorg.fink.data.entities.MediaCollection;
//import org.noorg.fink.data.entities.Tag;
//import org.noorg.fink.data.repository.MediaRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.neo4j.conversion.ResultConverter;
//import org.springframework.data.neo4j.support.Neo4jTemplate;
//import org.springframework.data.neo4j.support.node.Neo4jHelper;
//import org.springframework.data.neo4j.support.query.CypherQueryEngine;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.transaction.BeforeTransaction;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.google.common.collect.ImmutableList;
//
//@ContextConfiguration(locations = "/spring/testContext.xml")
//@RunWith(SpringJUnit4ClassRunner.class)
//@Transactional
//public class CypherTest {
//
//	@Autowired
//	private MediaRepository mediaRepository;
//
//	@Autowired
//	private Neo4jTemplate template;
//
//	@Rollback(false)
//	@BeforeTransaction
//	public void clearDatabase() {
//		Neo4jHelper.cleanDb(template);
//	}
//
//	private void createSomeCollections() {
//		MediaCollection c = new MediaCollection("test");
//		c.addItem(new Image("a", "a", "a", "a"));
//		c.addItem(new Image("a", "a", "a", "a"));
//		c.addItem(new Image("a", "a", "a", "a"));
//		c.addTag(new Tag("tagged"));
//		c.addTag(new Tag("by"));
//		c.addTag(new Tag("artist"));
//		mediaRepository.save(c);
//	}
//	
//	@Test
//	public void shouldExecuteCypherQuery() {
//		createSomeCollections();
//		
//		MediaCollection collection = mediaRepository.findCollectionByTitle("test");
//
//		CypherQueryEngine engine = new CypherQueryEngine(template.getGraphDatabaseService());
//		QueryResult<Map<String, Object>> result = engine.query( "start collection=(%collection) match (collection)-[:PART_OF]->(image) return image", MapUtil.map("collection", collection.getNodeId()));
//		
//		ConvertedResult<Image> res = result.to(Image.class, new ResultConverter<Map<String, Object>, Image>() {
//			@Override
//			public Image convert(Map<String, Object> value, Class<Image> type) {
//				return new Image();
//			}
//		});
//		ImmutableList<Image> l = ImmutableList.copyOf(res.iterator());
//		
//		assertEquals(3, l.size());
//	}
//	
//}
