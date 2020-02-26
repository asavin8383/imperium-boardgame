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
import repositories.PasdDetailResultRepo;
import services.DetailResultService;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class AnonimyzerDetailResultService implements DetailResultService<AnonymizerAnalysisResult, PasdDetailResult> {

	private final PasdDetailResultRepo pasdDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PASD;
	}

	@Override
	public PasdDetailResult create(AnonymizerAnalysisResult analysisResult){
		PasdDetailResult pasdDetailResult = new PasdDetailResult();
		fill(pasdDetailResult, analysisResult);
		return pasdDetailResult;
	}

	@Override
	public PasdDetailResult getOrCreate(Result result, AnonymizerAnalysisResult analysisResult) {
		PasdDetailResult pasdDetailResult = pasdDetailResultRepo.findById(result.getId()).orElseGet(PasdDetailResult::new);
		pasdDetailResult.setResult(result);
		fill(pasdDetailResult, analysisResult);
		return pasdDetailResult;
	}

	private void fill(PasdDetailResult pasdDetailResult, AnonymizerAnalysisResult analysisResult) {
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
	public String getErrorText(AnonymizerAnalysisResult analysisResult) {
		return analysisResult.getStubScoreInfo();
	}
}
