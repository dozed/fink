package org.noorg.fink.data.repository;

import java.util.List;

import org.noorg.fink.data.entities.MediaCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.DirectGraphRepositoryFactory;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;

@Repository
public class MediaRepository {
	
	private GraphRepository<MediaCollection> repository;

	@Autowired
	public void setGraphRepositoryFactory(DirectGraphRepositoryFactory graphRepositoryFactory) {
		repository = graphRepositoryFactory.createGraphRepository(MediaCollection.class);		
	}

	public List<MediaCollection> getCollections() {
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

	public long countCollections() {
		return repository.count();
	}
	
	public void save(MediaCollection media) {
		repository.save(media);
	}
	
}
