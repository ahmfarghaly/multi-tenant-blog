package dev.ams.cloud.multitenant.test;

import java.time.LocalDateTime;

import com.github.javafaker.Faker;

import dev.ams.cloud.multitenant.model.Post;
import dev.ams.cloud.multitenant.model.User;

public class EntityFactory {

	final Faker faker = new Faker();
	
	public User createUser() {
		return User.builder().name(faker.name().fullName()).build();
	}
	
	public Post createPost(User user) {
		return Post.builder().user(user).content(faker.shakespeare().hamletQuote()).date(LocalDateTime.now()).build();
	}
}
