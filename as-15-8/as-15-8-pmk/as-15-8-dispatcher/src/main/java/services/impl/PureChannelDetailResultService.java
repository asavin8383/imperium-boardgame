package services.impl;

import analysis.PureChannelAnalysisResult;
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
public class PureChannelDetailResultService implements DetailResultService<PureChannelAnalysisResult, PasdDetailResult> {

	private final PasdDetailResultRepo pasdDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PASD;
	}

	@Override
	public PasdDetailResult create(PureChannelAnalysisResult pureChannelAnalysisResult) {
		PasdDetailResult pasdDetailResult = new PasdDetailResult();
		fill(pasdDetailResult, pureChannelAnalysisResult);
		return pasdDetailResult;
	}

	@Override
	public PasdDetailResult getOrCreate(Result result, PureChannelAnalysisResult pureChannelAnalysisResult) {
		PasdDetailResult pasdDetailResult = pasdDetailResultRepo.findById(result.getId()).orElseGet(PasdDetailResult::new);
		pasdDetailResult.setResult(result);
		fill(pasdDetailResult, pureChannelAnalysisResult);
		return pasdDetailResult;
	}

	private void fill(PasdDetailResult pasdDetailResult, PureChannelAnalysisResult pureChannelAnalysisResult) {
		pasdDetailResult.setHttpStatus(pureChannelAnalysisResult.getHttpStatus());
		pasdDetailResult.setHttpHeaders(pureChannelAnalysisResult.getHttpHeaders());
		pasdDetailResult.setResponseErrorCode(pureChannelAnalysisResult.getResponseErrorCode());
		pasdDetailResult.setResponseError(pureChannelAnalysisResult.getResponseError());
		pasdDetailResult.setPageSize(pureChannelAnalysisResult.getPageSize());
		pasdDetailResult.setKeyWordsCount(pureChannelAnalysisResult.getKeyWordsCount());
		pasdDetailResult.setLinkCount(pureChannelAnalysisResult.getLinkCount());
		pasdDetailResult.setDomainNameCount(pureChannelAnalysisResult.getDomainNameCount());
		pasdDetailResult.setPageUrlFinal(pureChannelAnalysisResult.getPageUrlFinal());
		pasdDetailResult.setSimilarityOriginPercent(pureChannelAnalysisResult.getSimilarityOriginPercent());
		pasdDetailResult.setRedirectionDetected(pureChannelAnalysisResult.getRedirectionDetected());
		pasdDetailResult.setResultNLP(pureChannelAnalysisResult.getResultNLP());
		pasdDetailResult.setForbiddenFinalUrl(pureChannelAnalysisResult.getForbiddenFinalUrl());
	}

	@Override
	public void save(EntityManager entityManager, DetailResult pasdDetailResult) {
		PasdDetailResult detailResult = (PasdDetailResult) pasdDetailResult;
		String sql = "insert into results.pasd_detail_results " +
			"(result_id, domain_name_count, forbidden_final_url, " +
			"http_headers, http_status, " +
			"key_words_count, link_count, page_size, page_url_final, " +
			"redirection_detected, response_error, response_error_code, " +
			"result_nlp, similarity_origin_percent) " +
			"values " +
			"(:id, :domainNameCount, :forbiddenFinalUrl, " +
			":httpHeaders, :httpStatus, " +
			":keyWordsCount, :linkCount, :pageSize, :pageUrlFinal, " +
			":redirectionDetected, :responseError, :responseErrorCode, " +
			":resultNlp, :similarityOriginPercent) " +
			"on conflict(result_id) do update " +
			"set " +
			"result_id = :id, " +
			"domain_name_count = :domainNameCount, " +
			"forbidden_final_url = :forbiddenFinalUrl, " +
			"http_headers = :httpHeaders, " +
			"http_status = :httpStatus, " +
			"key_words_count = :keyWordsCount, " +
			"link_count = :linkCount, " +
			"page_size = :pageSize, " +
			"page_url_final = :pageUrlFinal, " +
			"redirection_detected = :redirectionDetected, " +
			"response_error = :responseError, " +
			"response_error_code = :responseErrorCode, " +
			"result_nlp = :resultNlp, " +
			"similarity_origin_percent = :similarityOriginPercent";
		NativeQuery nativeQuery = entityManager.createNativeQuery(sql).unwrap(NativeQuery.class);
		nativeQuery.setParameter("id", detailResult.getId());
		nativeQuery.setParameter("domainNameCount", detailResult.getDomainNameCount(), IntegerType.INSTANCE);
		nativeQuery.setParameter("forbiddenFinalUrl", detailResult.getForbiddenFinalUrl(), BooleanType.INSTANCE);
		nativeQuery.setParameter("httpHeaders", detailResult.getHttpHeaders(), StringType.INSTANCE);
		nativeQuery.setParameter("httpStatus", detailResult.getHttpStatus(), IntegerType.INSTANCE);
		nativeQuery.setParameter("keyWordsCount", detailResult.getKeyWordsCount(), IntegerType.INSTANCE);
		nativeQuery.setParameter("linkCount", detailResult.getLinkCount(), IntegerType.INSTANCE);
		nativeQuery.setParameter("pageSize", detailResult.getPageSize(), IntegerType.INSTANCE);
		nativeQuery.setParameter("pageUrlFinal", detailResult.getPageUrlFinal(), StringType.INSTANCE);
		nativeQuery.setParameter("redirectionDetected", detailResult.getRedirectionDetected(), BooleanType.INSTANCE);
		nativeQuery.setParameter("responseError", detailResult.getResponseError(), BooleanType.INSTANCE);
		nativeQuery.setParameter("responseErrorCode", detailResult.getResponseErrorCode(), StringType.INSTANCE);
		nativeQuery.setParameter("resultNlp", detailResult.getResultNLP(), StringType.INSTANCE);
		nativeQuery.setParameter("similarityOriginPercent", detailResult.getSimilarityOriginPercent(), IntegerType.INSTANCE);
		nativeQuery.executeUpdate();
	}

	@Override
	public String getErrorText(PureChannelAnalysisResult pureChannelAnalysisResult) {
		return pureChannelAnalysisResult.getResponseErrorCode();
	}
}
