package model.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import model.Views;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Creation date: 06.09.2019
 * Author: asavin
 */
@Entity
@Table(schema = "portal", name = "access_tools_categories")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccessToolsCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "access_tools_categories_generator")
    @SequenceGenerator(name="access_tools_categories_generator", schema = "portal", sequenceName = "access_tools_categories_id_seq", allocationSize=1)
    @JsonView(Views.Id.class)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @Column(unique = true, nullable = false)
    @JsonView(Views.Brief.class)
    private String name;

    @Column(name = "parent_id")
    @JsonView(Views.Full.class)
    private AccessToolsCategory parent;

}
