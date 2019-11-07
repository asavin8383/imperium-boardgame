package services.impl;

import analysis.AnonymizerAnalysisResult;
import enums.CheckUnitJobResult;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DetailResult;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.DetailResultRepo;
import repositories.ResultRepo;
import services.AnalysisResultService;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class AnonymizerAnalysisResultService implements AnalysisResultService<AnonymizerAnalysisResult> {

	private final DetailResultRepo detailVpnRepo;
	private final ResultRepo resultRepo;

	@Override
	public void processResult(Result result, AnonymizerAnalysisResult analysisResult) {

		DetailResult detailResult = new DetailResult();

		detailResult.setResult(result);

		detailResult.setHttpStatus(analysisResult.getHttpStatus());
		detailResult.setHttpStatusEtalon(analysisResult.getHttpStatusEtalon());
		detailResult.setHttpHeaders(analysisResult.getHttpHeaders());
		detailResult.setHttpHeadersEtalon(analysisResult.getHttpHeadersEtalon());
		detailResult.setResponseErrorCode(analysisResult.getErrorCode());
		detailResult.setPageSize(analysisResult.getPageSize());
		detailResult.setUseEtalon(analysisResult.getUseEtalon());
		detailResult.setResponseErrorCodeEtalon(analysisResult.getEtalonErrorCode());
		detailResult.setPageSizeEtalon(analysisResult.getEtalonPageSize());
		detailResult.setPageUrlFinal(analysisResult.getFinalUrl());
		detailResult.setStubUrl(analysisResult.getStubUrl());
		detailResult.setKeyWordsCount(analysisResult.getKeyWordsCount());
		detailResult.setLinkCount(analysisResult.getLinkCount());
		detailResult.setDomainNameCount(analysisResult.getDomainNameCount());
		detailResult.setStubScoreInfo(analysisResult.getStubScoreInfo());
		detailResult.setSimilarityOriginPercent(analysisResult.getSimilarityPercent());

		detailResult.setResponseError(analysisResult.getErrorCode() != null);
		detailResult.setRedirectionDetected(analysisResult.getRedirectionDetected());
		detailResult.setResultNLP(analysisResult.getResultNLP());

		detailVpnRepo.save(detailResult);

	}
}
