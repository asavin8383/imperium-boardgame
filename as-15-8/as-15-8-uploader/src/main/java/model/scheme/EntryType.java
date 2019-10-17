package model.scheme;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(schema="sor", name="entrytype")
@Data
public class EntryType implements Serializable {

	@Id
	private Long id;

	@Column(nullable=false)
	private String dsc;
}
