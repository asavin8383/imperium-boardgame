package model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Результаты выполнения мероприятия
 */

@Entity
@Table(schema = "portal", name = "detail_results")
@Data
public class DetailResultsVpn implements Serializable  {

    private static final long serialVersionUID = 666L;

    @Id
    @GeneratedValue(strategy=GenerationType.TABLE)
    @Column(name="id", nullable=false, updatable=false, columnDefinition="bigserial")
    private Long id;

    @Column(name="response_error_code")
    private String responseErrorCode;

    @Column(name="response_error")
    private Boolean responseError;

    @Column(name="http_status")
    private Integer httpStatus;

    @Column(name="page_size")
    private Integer pageSize;

    @Column(name="page_size_etalon")
    private Integer pageSizeEtalon;

    @Column(name="key_words")
    private Integer keyWordsCount;

    @Column(name="link_count")
    private Integer linkCount;

    @Column(name="domain_name_count")
    private Integer domainNameCount;

    @Column(name="page_url_final")
    private String pageUrlFinal;

    @Column(name="similarity_origin_percent")
    private Integer similarityOriginPercent;
}