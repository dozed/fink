package org.noorg.fink.data.repository;

import org.noorg.fink.data.entities.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.DirectGraphRepositoryFactory;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageRepository {
	
	private GraphRepository<Image> repository;;
	
	@Autowired
	public void setGraphRepositoryFactory(DirectGraphRepositoryFactory graphRepositoryFactory) {
		repository = graphRepositoryFactory.createGraphRepository(Image.class);		
	}

	public Iterable<Image> findAll() {
		return repository.findAll();
	}
	
	public Image findImage(String uuid) {
		return repository.findByPropertyValue("uuid", uuid);
	}

	@Transactional
	public Image addImage(String title, String image, String medium, String thumb) {
		return repository.save(new Image(title, image, medium, thumb));
	}
	
}
