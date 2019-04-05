package model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

/**
 * Поисковая система
 * @author asavin
 *
 */

@Entity
@Table(schema="portal",name="search_systems")
@Data
public class SearchSystem implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_systems_generator")
	@SequenceGenerator(name="search_systems_generator", schema = "portal", sequenceName = "search_systems_id_seq", allocationSize=1)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	
	private String name;
}
