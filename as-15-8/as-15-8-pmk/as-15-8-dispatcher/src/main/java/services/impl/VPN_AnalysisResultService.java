package services.impl;

import analysis.VpnAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.DetailResultRepo;
import repositories.ResultRepo;
import services.AnalysisResultService;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VPN_AnalysisResultService implements AnalysisResultService<VpnAnalysisResult> {

	private final DetailResultRepo detailResultRepo;

	@Override
	public void processResult(Result result, VpnAnalysisResult analysisResult) {

		DetailResult detailResult = new DetailResult();

		detailResult.setResult(result);

		detailResult.setHttpStatus(analysisResult.getHttpStatus());
		detailResult.setHttpStatusEtalon(analysisResult.getHttpStatusEtalon());
		detailResult.setHttpHeaders(analysisResult.getHttpHeaders());
		detailResult.setHttpHeadersEtalon(analysisResult.getHttpHeadersEtalon());
		detailResult.setResponseErrorCode(analysisResult.getResponseErrorCode());
		detailResult.setResponseErrorCodeEtalon(analysisResult.getResponseErrorCodeEtalon());
		detailResult.setResponseError(analysisResult.getResponseError());
		detailResult.setUseEtalon(analysisResult.getUseEtalon());
		detailResult.setPageSize(analysisResult.getPageSize());
		detailResult.setPageSizeEtalon(analysisResult.getPageSizeEtalon());
		detailResult.setKeyWordsCount(analysisResult.getKeyWordsCount());
		detailResult.setLinkCount(analysisResult.getLinkCount());
		detailResult.setDomainNameCount(analysisResult.getDomainNameCount());
		detailResult.setPageUrlFinal(analysisResult.getPageUrlFinal());
		detailResult.setPageUrlFinalEtalon(analysisResult.getPageUrlFinalEtalon());
		detailResult.setStubUrl(analysisResult.getStubUrl());
		detailResult.setSimilarityOriginPercent(analysisResult.getSimilarityOriginPercent());
		detailResult.setStubScoreInfo(analysisResult.getStubScoreInfo());
		detailResult.setRedirectionDetected(analysisResult.getRedirectionDetected());
		detailResult.setResultNLP(analysisResult.getResultNLP());
		detailResult.setForbiddenFinalUrl(analysisResult.getForbiddenFinalUrl());

		detailResultRepo.save(detailResult);

	}
}
