package model.scheme;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Table(schema="sor", name="entrytype")
@Data
public class EntryType implements Serializable {

	@Id
	private String id;

	@Column(nullable=false)
	private String dsc;
}
