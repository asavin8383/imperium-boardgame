package model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Creation date: 30.05.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "search_system_parameters")
@Data
public class SearchSystemParameters  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "search_system_parameters_generator")
    @SequenceGenerator(name="search_system_parameters_generator", schema = "portal", sequenceName = "search_system_parameters_id_seq", allocationSize=1)
    @Column(name="id", nullable=false, updatable=false)
    private Long id;

    @OneToOne(optional = false, cascade=CascadeType.ALL)
    @JsonIgnore
    private AccessTool accessTool;

    private String searchSystemUrl;
    private String inputSearchFieldXpathId;
    private String inputSearchFieldCssSelector;
    private String buttonSearchFieldXpathId;
    private String buttonSearchFieldCssSelector;
}
