package model.erdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */

@Entity
@Table(schema = "sa", name = "decision")
@Immutable
@Getter
public class Decision {

    @Id
    private Long id;

    private LocalDateTime date;
    @Column(name = "org")
    private String organization;
    @Column(name = "number")
    private String decisionNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "content_id", referencedColumnName = "id")
    @JsonIgnore
    private ERDI erdi;

}
