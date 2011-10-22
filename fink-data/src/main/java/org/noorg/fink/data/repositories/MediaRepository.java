package org.noorg.fink.data.repositories;

import java.util.List;

import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.MapUtil;
import org.noorg.fink.data.entities.Image;
import org.noorg.fink.data.entities.MediaCollection;
import org.noorg.fink.data.repositories.internal.MediaRepositoryInternal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.query.QueryEngine;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

@Repository
public class MediaRepository {

	@Autowired
	private MediaRepositoryInternal repository;

	@Autowired
	private Neo4jTemplate template;

	public List<MediaCollection> findAll() {
		return ImmutableList.copyOf(repository.findAll());
	}

	public List<MediaCollection> findCollections() {
		return ImmutableList.copyOf(repository.findAll());
	}

	@Transactional
	public MediaCollection createCollection(String title) {
		return repository.save(new MediaCollection(title));
	}

	public MediaCollection findCollection(String uuid) {
		return repository.findByPropertyValue("uuid", uuid);
	}

	public MediaCollection findCollectionByTitle(String title) {
		return repository.findByPropertyValue("title", title);
	}

	public MediaCollection findCollectionByShortlink(String shortlink) {
		return repository.findByPropertyValue("shortlink", shortlink);
	}

	public long countCollections() {
		return repository.count();
	}

	public void save(MediaCollection media) {
		repository.save(media);
	}

	public List<Image> getCollectionItems(MediaCollection media) {
		QueryEngine<Image> engine = template.queryEngineFor(QueryType.Cypher);
		EndResult<Image> r = engine.query(
				"start n=node({id}) match (n)-[p:PART_OF]->(image) return image order by p.sorting?",
				MapUtil.map("id", media.getId())).to(Image.class);
		return ImmutableList.copyOf(r);
	}

	@Transactional
	public void sortImages(MediaCollection media, String[] uuids) {
		int valid = 0;

		// check if all items occur in the incoming array
		for (String uuid : uuids) {
			for (Image i : media.getItems()) {
				if (i.getUuid().equals(uuid)) {
					valid++;
					break;
				}
			}
		}

		if (valid == media.getItems().size()) {
			int sorting = 0;

			for (String uuid : uuids) {
				for (Image img : media.getItems()) {
					if (img.getUuid().equals(uuid)) {
						Relationship r = template.getRelationshipBetween(media, img, "PART_OF");
						r.setProperty("sorting", sorting++);
					}
				}
			}
		}
	}

}
