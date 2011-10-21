package org.noorg.fink.data.entities;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Image extends ContentItem {

	private String image;
	private String medium;
	private String thumb;

	public Image() {}
	
	public Image(String title, String image, String medium, String thumb) {
		setUuid(UUID.randomUUID().toString());
		setDate(new DateTime());
		setTitle(title);
		this.image = image;
		this.medium = medium;
		this.thumb = thumb;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

}
