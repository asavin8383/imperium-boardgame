package model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Результаты выполнения мероприятия
 */

@Entity
@Table(schema = "results", name = "pasd_detail_results")
@Data
public class PasdDetailResult implements Serializable  {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @OneToOne(optional = false)
    @MapsId
    private Result result;

    @Column(name="response_error_code")
    private String responseErrorCode;

    @Column(name="response_error_code_etalon")
    private String responseErrorCodeEtalon;

    @Column(name="response_error")
    private Boolean responseError;

    @Column(name="use_etalon")
    private Boolean useEtalon;

    @Column(name="http_status")
    private Integer httpStatus;

    @Column(name="http_status_etalon")
    private Integer httpStatusEtalon;

    @Column(name="http_headers")
    private String httpHeaders;

    @Column(name="http_headers_etalon")
    private String httpHeadersEtalon;

    @Column(name="page_size")
    private Integer pageSize;

    @Column(name="page_size_etalon")
    private Integer pageSizeEtalon;

    @Column(name="key_words_count")
    private Integer keyWordsCount;

    @Column(name="link_count")
    private Integer linkCount;

    @Column(name="domain_name_count")
    private Integer domainNameCount;

    @Column(name="page_url_final")
    private String pageUrlFinal;

    @Column(name="page_url_final_etalon")
    private String pageUrlFinalEtalon;

    @Column(name="redirection_detected")
    private Boolean redirectionDetected;

    @Column(name="forbidden_final_url")
    private Boolean forbiddenFinalUrl;

    @Column(name="stub_url")
    private String stubUrl;

    @Column(name="similarity_origin_percent")
    private Integer similarityOriginPercent;

    @Column(name="stub_score_info")
    private String stubScoreInfo;

    @Column(name="result_nlp")
    private String resultNLP;
}