package services.impl;

import analysis.VpnAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.PasdDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PasdDetailResultRepo;
import services.DetailResultService;


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
	public void save(DetailResult pasdDetailResult) {
		PasdDetailResult detailResult = (PasdDetailResult) pasdDetailResult;
		pasdDetailResultRepo.upsert(
				detailResult.getId(),
				detailResult.getDomainNameCount(),
				detailResult.getForbiddenFinalUrl(),
				detailResult.getHttpHeaders(),
				detailResult.getHttpHeadersEtalon(),
				detailResult.getHttpStatus(),
				detailResult.getHttpStatusEtalon(),
				detailResult.getKeyWordsCount(),
				detailResult.getLinkCount(),
				detailResult.getPageSize(),
				detailResult.getPageSizeEtalon(),
				detailResult.getPageUrlFinal(),
				detailResult.getPageUrlFinalEtalon(),
				detailResult.getRedirectionDetected(),
				detailResult.getResponseError(),
				detailResult.getResponseErrorCode(),
				detailResult.getResponseErrorCodeEtalon(),
				detailResult.getResultNLP(),
				detailResult.getSimilarityOriginPercent(),
				detailResult.getStubScoreInfo(),
				detailResult.getStubUrl(),
				detailResult.getUseEtalon()
		);
	}

	@Override
	public String getErrorText(VpnAnalysisResult analysisResult) {
		return analysisResult.getStubScoreInfo();
	}
}
