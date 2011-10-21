package org.noorg.fink.data.repositories;

import org.noorg.fink.data.entities.Tag;
import org.noorg.fink.data.repositories.internal.TagRepositoryInternal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagRepository {

	@Autowired
	private TagRepositoryInternal repository;
	
	public Tag findTag(String name) {
		return repository.findByPropertyValue("name", name);
	}

	@Transactional
	public Tag createTag(String name) {
		return repository.save(new Tag(name));
	}
	
}
