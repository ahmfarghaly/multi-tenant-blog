package dev.ams.cloud.multitenant.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.ams.cloud.multitenant.model.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
