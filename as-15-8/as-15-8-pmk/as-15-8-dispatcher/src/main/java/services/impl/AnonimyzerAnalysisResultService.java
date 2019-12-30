package services.impl;

import analysis.AnonymizerAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.PasdDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.AnalysisResultService;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class AnonimyzerAnalysisResultService implements AnalysisResultService<AnonymizerAnalysisResult> {

	@Override
	public CheckType getCheckType() {
		return CheckType.PASD;
	}

	@Override
	public DetailResult createDetails(Result result, AnonymizerAnalysisResult analysisResult) {

		PasdDetailResult pasdDetailResult = new PasdDetailResult();

		pasdDetailResult.setResult(result);

		pasdDetailResult.setHttpStatus(analysisResult.getHttpStatus());
		pasdDetailResult.setHttpStatusEtalon(analysisResult.getHttpStatusEtalon());
		pasdDetailResult.setHttpHeaders(analysisResult.getHttpHeaders());
		pasdDetailResult.setHttpHeadersEtalon(analysisResult.getHttpHeadersEtalon());
		pasdDetailResult.setResponseErrorCode(analysisResult.getErrorCode());
		pasdDetailResult.setPageSize(analysisResult.getPageSize());
		pasdDetailResult.setUseEtalon(analysisResult.getUseEtalon());
		pasdDetailResult.setResponseErrorCodeEtalon(analysisResult.getEtalonErrorCode());
		pasdDetailResult.setPageSizeEtalon(analysisResult.getEtalonPageSize());
		pasdDetailResult.setPageUrlFinal(analysisResult.getFinalUrl());
		pasdDetailResult.setStubUrl(analysisResult.getStubUrl());
		pasdDetailResult.setKeyWordsCount(analysisResult.getKeyWordsCount());
		pasdDetailResult.setLinkCount(analysisResult.getLinkCount());
		pasdDetailResult.setDomainNameCount(analysisResult.getDomainNameCount());
		pasdDetailResult.setStubScoreInfo(analysisResult.getStubScoreInfo());
		pasdDetailResult.setSimilarityOriginPercent(analysisResult.getSimilarityPercent());

		pasdDetailResult.setResponseError(analysisResult.getErrorCode() != null);
		pasdDetailResult.setRedirectionDetected(analysisResult.getRedirectionDetected());
		pasdDetailResult.setResultNLP(analysisResult.getResultNLP());

		return pasdDetailResult;
	}

	@Override
	public String getErrorText(AnonymizerAnalysisResult analysisResult) {
		return analysisResult.getStubScoreInfo();
	}
}
