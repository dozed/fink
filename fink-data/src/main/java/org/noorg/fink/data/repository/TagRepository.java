package org.noorg.fink.data.repository;

import org.noorg.fink.data.entities.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.DirectGraphRepositoryFactory;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagRepository {

	private GraphRepository<Tag> repository;
	
	@Autowired
	public TagRepository(DirectGraphRepositoryFactory graphRepositoryFactory) {
		repository = graphRepositoryFactory.createGraphRepository(Tag.class);		
	}
	
	public Tag findTag(String name) {
		return repository.findByPropertyValue("name", name);
	}

	@Transactional
	public Tag createTag(String name) {
		return repository.save(new Tag(name));
	}
	
}
