package dev.ams.cloud.multitenant.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
public class Post implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -19043065325479503L;
	@Id
	@GeneratedValue
	Long id;
	String title;
	LocalDateTime date;
	String content;
	User user;
}
