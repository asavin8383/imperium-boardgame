package services.impl;

import analysis.AnonymizerAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.PasdDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PasdDetailResultRepo;
import repositories.ResultRepo;
import services.AnalysisResultService;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class AnonymizerAnalysisResultService implements AnalysisResultService<AnonymizerAnalysisResult> {

	private final PasdDetailResultRepo detailVpnRepo;
	private final ResultRepo resultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PASD;
	}

	@Override
	public void saveResult(Result result, AnonymizerAnalysisResult analysisResult) {

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

		detailVpnRepo.save(pasdDetailResult);

	}

	@Override
	public String getErrorText(AnonymizerAnalysisResult analysisResult) {
		return analysisResult.getStubScoreInfo();
	}
}
