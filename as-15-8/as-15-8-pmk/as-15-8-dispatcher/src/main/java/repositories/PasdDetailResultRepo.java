package repositories;

import model.PasdDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface PasdDetailResultRepo extends JpaRepository<PasdDetailResult, Long> {

    @Modifying
    @Query(
            value = "insert into results.pasd_detail_results " +
                        "(result_id, domain_name_count, forbidden_final_url, " +
                        "http_headers, http_headers_etalon, http_status, http_status_etalon, " +
                        "key_words_count, link_count, page_size, page_size_etalon, page_url_final, page_url_final_etalon, " +
                        "redirection_detected, response_error, response_error_code, response_error_code_etalon, " +
                        "result_nlp, similarity_origin_percent, stub_score_info, stub_url, use_etalon) " +
                    "values " +
                        "(:resultId, :domainNameCount, :forbiddenFinalUrl, " +
                        ":httpHeaders, :httpHeadersEtalon, :httpStatus, :httpStatusEtalon, " +
                        ":keyWordsCount, :linkCount, :pageSize, :pageSizeEtalon, :pageUrlFinal, :pageUrlFinalEtalon, " +
                        ":redirectionDetected, :responseError, :responseErrorCode, :responseErrorCodeEtalon, " +
                        ":resultNlp, :similarityOriginPercent, :stubScoreInfo, :stubUrl, :useEtalon) " +
                    "on conflict do update " +
                    "set " +
                        "result_id = :id, " +
                        "domain_name_count = :domainNameCount, " +
                        "forbidden_final_url = :forbiddenFinalUrl, " +
                        "http_headers = :httpHeaders, " +
                        "http_headers_etalon = :httpHeadersEtalon, " +
                        "http_status = :httpStatus, " +
                        "http_status_etalon = :httpStatusEtalon, " +
                        "key_words_count = :keyWordsCount, " +
                        "link_count = :linkCount, " +
                        "page_size = :pageSize, " +
                        "page_size_etalon = :pageSizeEtalon, " +
                        "page_url_final = :pageUrlFinal, " +
                        "page_url_final_etalon = :pageUrlFinalEtalon, " +
                        "redirection_detected = :redirectionDetected, " +
                        "response_error = :responseError, " +
                        "response_error_code = :responseErrorCode, " +
                        "response_error_code_etalon = :responseErrorCodeEtalon, " +
                        "result_nlp = :resultNlp, " +
                        "similarity_origin_percent = :similarityOriginPercent, " +
                        "stub_score_info = :stubScoreInfo, " +
                        "stub_url = :stubUrl, " +
                        "use_etalon = :useEtalon",
            nativeQuery = true
    )
    int upsert(
            @Param("id") Long id,
            @Param("domainNameCount") Integer domainNameCount,
            @Param("forbiddenFinalUrl") Boolean forbiddenFinalUrl,
            @Param("httpHeaders") String httpHeaders,
            @Param("httpHeadersEtalon") String httpHeadersEtalon,
            @Param("httpStatus") Integer httpStatus,
            @Param("httpStatusEtalon") Integer httpStatusEtalon,
            @Param("keyWordsCount") Integer keyWordsCount,
            @Param("linkCount") Integer linkCount,
            @Param("pageSize") Integer pageSize,
            @Param("pageSizeEtalon") Integer pageSizeEtalon,
            @Param("pageUrlFinal") String pageUrlFinal,
            @Param("pageUrlFinalEtalon") String pageUrlFinalEtalon,
            @Param("redirectionDetected") Boolean redirectionDetected,
            @Param("responseError") Boolean responseError,
            @Param("responseErrorCode") String responseErrorCode,
            @Param("responseErrorCodeEtalon") String responseErrorCodeEtalon,
            @Param("resultNlp") String resultNlp,
            @Param("similarityOriginPercent") Integer similarityOriginPercent,
            @Param("stubScoreInfo") String stubScoreInfo,
            @Param("stubUrl") String stubUrl,
            @Param("useEtalon") Boolean useEtalon
    );

}
