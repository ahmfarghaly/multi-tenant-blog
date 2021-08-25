package dev.ams.cloud.multitenant.model.config;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="DATA_SRC_CFG")
@Getter
@Setter
public class DataSourceConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8676802577370334602L;

	@Id
    @GeneratedValue
    @Column(name="DS_ID")
    private Long id;
	@Column(name="DS_NAME")
    private String name;
	@Column(name="DS_URL")
    private String url;
	@Column(name="DS_user")
    private String username;
	@Column(name="DS_password")
    private String password;
	@Column(name="DS_driver")
    private String driverClassName;
	@Column(name="DS_init")
    private boolean initialize;
}
