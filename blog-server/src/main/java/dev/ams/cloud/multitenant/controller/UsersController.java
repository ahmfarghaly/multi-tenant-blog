package dev.ams.cloud.multitenant.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.ams.cloud.multitenant.dao.UserRepository;
import dev.ams.cloud.multitenant.model.User;

@RestController
@RequestMapping(value="/users",produces = MediaType.APPLICATION_JSON_VALUE)
public class UsersController {

	@Autowired
	UserRepository repository;
	
	@GetMapping
	public List<User> getAll() {
		return repository.findAll();
	}
	
	@PostMapping
	public User create(@RequestBody User user) {
		return repository.save(user);
	}
}
