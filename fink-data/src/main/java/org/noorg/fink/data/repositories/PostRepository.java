package org.noorg.fink.data.repositories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.noorg.fink.data.entities.Post;
import org.noorg.fink.data.entities.Tag;
import org.noorg.fink.data.repositories.internal.PostRepositoryInternal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@Service
public class PostRepository {
	
	@Autowired
	private PostRepositoryInternal repository;

	public List<Post> getEntries() {
		return ImmutableList.copyOf(repository.findAll());
	}
	
	@Transactional
	public Post createPost(String title, String text, String author, String category, String tags) {
		Iterable<String> it = Splitter.on(',').trimResults().split(tags);
		Set<Tag> tagSet = new HashSet<Tag>();
		for (String t : it) {
			tagSet.add(new Tag(t));
		}
		return repository.save(new Post(title, text, author, null, tagSet));
	}
	
	public void save(Post post) {
		repository.save(post);
	}
	
	public Post findPost(int year, int month, int day, String title) {
		DateTime dt = new DateTime(year, month, day, 0, 0, 0, 0);
		DateTime dtNext = new DateTime(year, month, day+1, 0, 0, 0, 0);

		for (Post p : getEntries()) {
			if (p.getDate().isAfter(dt.getMillis()) && p.getDate().isBefore(dtNext.getMillis())) {
				return p;
			}
		}
		
		return null;
	}
	
	public Post findPostByUuid(String uuid) {
		return repository.findByPropertyValue("uuid", uuid);
	}
	
}
