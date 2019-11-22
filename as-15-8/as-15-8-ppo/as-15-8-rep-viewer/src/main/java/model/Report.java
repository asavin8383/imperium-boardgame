package model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 4:23
 */
@Entity
@Table(schema = "dm", name = "param_reports")
@Data
public class Report
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long rep_id;

    @Column
    Integer rep_tp_id;

    @Column
    String rep_nm;

    @Column
    Integer msr_prd_id;

    @Column
    LocalDate msr_prd_st_dttm;

    @Column
    LocalDateTime msr_prd_end_dttm;

    @Column
    String msr_prd_tp;

    @Column
    Integer msr_prd_tp_id;

    @Column
    String msr_prd_caption;

    @Column
    String mime;

    @ToString.Exclude
    @Column
    private byte[] data;

    @Column
    String rptdesign;

    @Column
    String format;

    @JsonRawValue
    @Column(columnDefinition = "jsonb")
    String params;

    @Column
    String username;

}
