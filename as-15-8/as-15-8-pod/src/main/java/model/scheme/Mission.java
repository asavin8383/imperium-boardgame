package model.scheme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(schema="sor",name="mission")
@Data
public class Mission {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="mission_generator")
    @SequenceGenerator(name="mission_generator", sequenceName="sor.mission_id_seq", allocationSize=1)
    private Long id;

    @Column(nullable=false, unique = true, name = "orig_id")
    private String origId;

    @Column(nullable=false, unique=true, name = "doc_num")
    private String docNum;

    @Column(nullable=false, name = "type_check")
    private Integer typeCheck;

    @Column(nullable=false, name = "date_approved")
    private Date dateApproved;
}
