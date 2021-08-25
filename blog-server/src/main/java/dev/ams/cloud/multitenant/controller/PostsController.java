package dev.ams.cloud.multitenant.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ams.cloud.multitenant.dao.PostRepository;
import dev.ams.cloud.multitenant.model.Post;

@RestController
@RequestMapping(value="/posts",produces = MediaType.APPLICATION_JSON_VALUE)
public class PostsController {

	@Autowired
	private PostRepository repository;
	
	@GetMapping
	public List<Post> getAll() {
		return repository.findAll();
	}
	
	@PostMapping
	public Post createPost(@RequestBody Post post) {
		return repository.save(post);
	}
}
