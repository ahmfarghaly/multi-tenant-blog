package dev.ams.cloud.multitenant.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.ams.cloud.multitenant.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
