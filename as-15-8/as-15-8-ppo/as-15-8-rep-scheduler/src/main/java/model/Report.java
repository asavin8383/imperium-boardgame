package model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
@Data
@Entity
@Table(schema = "dm", name = "reg_reports")
public class Report
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long rep_id;

    @Column
    private long rep_tp_id;

    @Column
    private String rep_nm;

    @Column
    private Timestamp msr_prd_st_dttm;

    @Column
    private Timestamp msr_prd_end_dttm;

    @Column
    private String msr_prd_tp;

    @Column
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column
    private Timestamp start_dttm;

    @Column
    private Timestamp finish_dttm;

    @Column
    private String mime;

    @Column
    private String format;

    @Column
    private String rptdesign;

    @Column
    private String reason;

    @Column
    private byte[] data;

}
