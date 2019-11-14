package model.scheme;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by san
 * Date: 04.11.2019
 */
@Entity
@Table(schema = "sor", name = "domain_mask_items")
@Data
public class DomainMaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String domainMask;
    @Column(nullable = false, unique = true)
    private String domainMaskItem;
}
