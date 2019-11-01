package model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * User: asinjavin
 * Date: 01.11.2019
 * Time: 12:56
 */
@Data
@Entity
@Table(schema = "dm", name = "v_api_reg_reports_table")
public class RegReportsTable
{
    @Column long repTpId;
    @Id long msr_prd_id;
    @Column String msr_prd_tp;
    @Column String msr_prd_caption;
    @Column Timestamp finish_dttm;

    @JsonRawValue
    @Column String statuses;

}
