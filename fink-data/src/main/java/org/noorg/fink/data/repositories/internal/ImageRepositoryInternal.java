package org.noorg.fink.data.repositories.internal;

import org.noorg.fink.data.entities.Image;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.NamedIndexRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepositoryInternal extends GraphRepository<Image>, NamedIndexRepository<Image> { }
