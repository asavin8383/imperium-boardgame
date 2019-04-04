package model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
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
	private Long id;
	
	private String name;
}
