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
	public CheckUnitJobResult processResult(AnonymizerAnalysisResult aRes) {

		DetailResult detailResult = new DetailResult();

		Result result = resultRepo.findById(aRes.getJobID())
				.orElseThrow(() -> AS_15_8_DispatcherException.logAndGet(log, String.format("Результат c ИД %d не найден в БД", aRes.getJobID())));

		detailResult.setResult(result);
		detailResult.setHttpStatus(aRes.getHttpStatus());
		detailResult.setHttpStatusEtalon(aRes.getHttpStatusEtalon());
		detailResult.setHttpHeaders(aRes.getHttpHeaders());
		detailResult.setHttpHeadersEtalon(aRes.getHttpHeadersEtalon());
		detailResult.setResponseErrorCode(aRes.getErrorCode());
		detailResult.setPageSize(aRes.getPageSize());
		detailResult.setUseEtalon(aRes.getUseEtalon());
		detailResult.setResponseErrorCodeEtalon(aRes.getEtalonErrorCode());
		detailResult.setPageSizeEtalon(aRes.getEtalonPageSize());
		detailResult.setPageUrlFinal(aRes.getFinalUrl());
		detailResult.setStubUrl(aRes.getStubUrl());
		detailResult.setKeyWordsCount(aRes.getKeyWordsCount());
		detailResult.setLinkCount(aRes.getLinkCount());
		detailResult.setDomainNameCount(aRes.getDomainNameCount());
		detailResult.setStubScoreInfo(aRes.getStubScoreInfo());
		detailResult.setSimilarityOriginPercent(aRes.getSimilarityPercent());

		detailResult.setResponseError(aRes.getErrorCode() != null);
		detailResult.setRedirectionDetected(aRes.getRedirectionDetected());
		detailResult.setResultNLP(aRes.getResultNLP());

		detailVpnRepo.save(detailResult);

		return aRes.getCheckResult();
	}
}
