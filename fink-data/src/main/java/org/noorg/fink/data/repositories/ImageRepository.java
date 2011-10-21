package org.noorg.fink.data.repositories;

import org.noorg.fink.data.entities.Image;
import org.noorg.fink.data.repositories.internal.ImageRepositoryInternal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ImageRepository {

	@Autowired
	private ImageRepositoryInternal repository;

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
