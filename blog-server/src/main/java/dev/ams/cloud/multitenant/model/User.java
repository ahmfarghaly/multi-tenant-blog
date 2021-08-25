package dev.ams.cloud.multitenant.model;

import java.io.Serializable;

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
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -15729200504961800L;
	@Id
	@GeneratedValue
	Long id;
	String name;
}
