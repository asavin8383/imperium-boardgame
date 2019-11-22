package model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 17:58
 */
@Entity
@Table(schema = "dm", name = "msr_prd_tp")
@Data
public class MsrPrdTp
{
    @Id Integer msrPrdTpId;

    @Column String nm;
}
