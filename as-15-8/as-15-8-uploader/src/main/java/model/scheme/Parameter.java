package model.scheme;

import lombok.Data;

import javax.persistence.*;


@Entity
@Table(schema="sor",name="parameters")
@Data
public class Parameter {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="parameters_generator")
    @SequenceGenerator(name="parameters_generator", sequenceName="sor.parameters_id_seq", allocationSize=1)
    @Column(name="id", nullable=false)
    private Long id;

    private String name;

    private String value;

    private Integer enabled;
}
