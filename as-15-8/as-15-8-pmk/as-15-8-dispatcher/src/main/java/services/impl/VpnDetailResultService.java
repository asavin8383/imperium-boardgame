package services.impl;

import analysis.VpnAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.PasdDetailResult;
import model.Result;
import model.enums.CheckType;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PasdDetailResultRepo;
import services.DetailResultService;

import javax.persistence.EntityManager;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VpnDetailResultService implements DetailResultService<VpnAnalysisResult, PasdDetailResult> {

	private final PasdDetailResultRepo pasdDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PASD;
	}

	@Override
	public PasdDetailResult create(VpnAnalysisResult vpnAnalysisResult) {
		PasdDetailResult pasdDetailResult = new PasdDetailResult();
		fill(pasdDetailResult, vpnAnalysisResult);
		return pasdDetailResult;
	}

	@Override
	public PasdDetailResult getOrCreate(Result result, VpnAnalysisResult vpnAnalysisResult) {
		PasdDetailResult pasdDetailResult = pasdDetailResultRepo.findById(result.getId()).orElseGet(PasdDetailResult::new);
		pasdDetailResult.setResult(result);
		fill(pasdDetailResult, vpnAnalysisResult);
		return pasdDetailResult;
	}

	private void fill(PasdDetailResult pasdDetailResult, VpnAnalysisResult analysisResult) {
		pasdDetailResult.setHttpStatus(analysisResult.getHttpStatus());
		pasdDetailResult.setHttpStatusEtalon(analysisResult.getHttpStatusEtalon());
		pasdDetailResult.setHttpHeaders(analysisResult.getHttpHeaders());
		pasdDetailResult.setHttpHeadersEtalon(analysisResult.getHttpHeadersEtalon());
		pasdDetailResult.setResponseErrorCode(analysisResult.getResponseErrorCode());
		pasdDetailResult.setResponseErrorCodeEtalon(analysisResult.getResponseErrorCodeEtalon());
		pasdDetailResult.setResponseError(analysisResult.getResponseError());
		pasdDetailResult.setUseEtalon(analysisResult.getUseEtalon());
		pasdDetailResult.setPageSize(analysisResult.getPageSize());
		pasdDetailResult.setPageSizeEtalon(analysisResult.getPageSizeEtalon());
		pasdDetailResult.setKeyWordsCount(analysisResult.getKeyWordsCount());
		pasdDetailResult.setLinkCount(analysisResult.getLinkCount());
		pasdDetailResult.setDomainNameCount(analysisResult.getDomainNameCount());
		pasdDetailResult.setPageUrlFinal(analysisResult.getPageUrlFinal());
		pasdDetailResult.setPageUrlFinalEtalon(analysisResult.getPageUrlFinalEtalon());
		pasdDetailResult.setStubUrl(analysisResult.getStubUrl());
		pasdDetailResult.setSimilarityOriginPercent(analysisResult.getSimilarityOriginPercent());
		pasdDetailResult.setStubScoreInfo(analysisResult.getStubScoreInfo());
		pasdDetailResult.setRedirectionDetected(analysisResult.getRedirectionDetected());
		pasdDetailResult.setResultNLP(analysisResult.getResultNLP());
		pasdDetailResult.setForbiddenFinalUrl(analysisResult.getForbiddenFinalUrl());
	}

	@Override
	public void save(EntityManager entityManager, DetailResult pasdDetailResult) {
		PasdDetailResult detailResult = (PasdDetailResult) pasdDetailResult;
		String sql = "insert into results.pasd_detail_results " +
			"(result_id, domain_name_count, forbidden_final_url, " +
			"http_headers, http_headers_etalon, http_status, http_status_etalon, " +
			"key_words_count, link_count, page_size, page_size_etalon, page_url_final, page_url_final_etalon, " +
			"redirection_detected, response_error, response_error_code, response_error_code_etalon, " +
			"result_nlp, similarity_origin_percent, stub_score_info, stub_url, use_etalon) " +
			"values " +
			"(:id, :domainNameCount, :forbiddenFinalUrl, " +
			":httpHeaders, :httpHeadersEtalon, :httpStatus, :httpStatusEtalon, " +
			":keyWordsCount, :linkCount, :pageSize, :pageSizeEtalon, :pageUrlFinal, :pageUrlFinalEtalon, " +
			":redirectionDetected, :responseError, :responseErrorCode, :responseErrorCodeEtalon, " +
			":resultNlp, :similarityOriginPercent, :stubScoreInfo, :stubUrl, :useEtalon) " +
			"on conflict(result_id) do update " +
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
			"use_etalon = :useEtalon";
		NativeQuery nativeQuery = entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
		nativeQuery.setParameter("id", detailResult.getId());
		nativeQuery.setParameter("domainNameCount", detailResult.getDomainNameCount(), IntegerType.INSTANCE);
		nativeQuery.setParameter("forbiddenFinalUrl", detailResult.getForbiddenFinalUrl(), BooleanType.INSTANCE);
		nativeQuery.setParameter("httpHeaders", detailResult.getHttpHeaders(), StringType.INSTANCE);
		nativeQuery.setParameter("httpHeadersEtalon", detailResult.getHttpHeadersEtalon(), StringType.INSTANCE);
		nativeQuery.setParameter("httpStatus", detailResult.getHttpStatus(), IntegerType.INSTANCE);
		nativeQuery.setParameter("httpStatusEtalon", detailResult.getHttpStatusEtalon(), IntegerType.INSTANCE);
		nativeQuery.setParameter("keyWordsCount", detailResult.getKeyWordsCount(), IntegerType.INSTANCE);
		nativeQuery.setParameter("linkCount", detailResult.getLinkCount(), IntegerType.INSTANCE);
		nativeQuery.setParameter("pageSize", detailResult.getPageSize(), IntegerType.INSTANCE);
		nativeQuery.setParameter("pageSizeEtalon", detailResult.getPageSizeEtalon(), IntegerType.INSTANCE);
		nativeQuery.setParameter("pageUrlFinal", detailResult.getPageUrlFinal(), StringType.INSTANCE);
		nativeQuery.setParameter("pageUrlFinalEtalon", detailResult.getPageUrlFinalEtalon(), StringType.INSTANCE);
		nativeQuery.setParameter("redirectionDetected", detailResult.getRedirectionDetected(), BooleanType.INSTANCE);
		nativeQuery.setParameter("responseError", detailResult.getResponseError(), BooleanType.INSTANCE);
		nativeQuery.setParameter("responseErrorCode", detailResult.getResponseErrorCode(), StringType.INSTANCE);
		nativeQuery.setParameter("responseErrorCodeEtalon", detailResult.getResponseErrorCodeEtalon(), StringType.INSTANCE);
		nativeQuery.setParameter("resultNlp", detailResult.getResultNLP(), StringType.INSTANCE);
		nativeQuery.setParameter("similarityOriginPercent", detailResult.getSimilarityOriginPercent(), IntegerType.INSTANCE);
		nativeQuery.setParameter("stubScoreInfo", detailResult.getStubScoreInfo(), StringType.INSTANCE);
		nativeQuery.setParameter("stubUrl", detailResult.getStubUrl(), StringType.INSTANCE);
		nativeQuery.setParameter("useEtalon", detailResult.getUseEtalon(), BooleanType.INSTANCE);
		nativeQuery.executeUpdate();
	}

	@Override
	public String getErrorText(VpnAnalysisResult analysisResult) {
		return analysisResult.getStubScoreInfo();
	}
}
