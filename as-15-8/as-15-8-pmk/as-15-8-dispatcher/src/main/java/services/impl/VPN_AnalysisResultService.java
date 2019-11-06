package services.impl;

import analysis.VpnAnalysisResult;
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


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VPN_AnalysisResultService implements AnalysisResultService<VpnAnalysisResult> {

	private final DetailResultRepo detailVpnRepo;
	private final ResultRepo resultRepo;

	@Override
	public CheckUnitJobResult processResult(VpnAnalysisResult aRes) {

		DetailResult detailResult = new DetailResult();

		Result result = resultRepo.findById(aRes.getJobID())
				.orElseThrow(() -> AS_15_8_DispatcherException.logAndGet(log, String.format("Результат c ИД %d не найден в БД", aRes.getJobID())));

		detailResult.setHttpStatus(aRes.getHttpStatus());
		detailResult.setHttpStatusEtalon(aRes.getHttpStatusEtalon());
		detailResult.setHttpHeaders(aRes.getHttpHeaders());
		detailResult.setHttpHeadersEtalon(aRes.getHttpHeadersEtalon());
		detailResult.setResponseErrorCode(aRes.getResponseErrorCode());
		detailResult.setResponseErrorCodeEtalon(aRes.getResponseErrorCodeEtalon());
		detailResult.setResponseError(aRes.getResponseError());
		detailResult.setUseEtalon(aRes.getUseEtalon());
		detailResult.setPageSize(aRes.getPageSize());
		detailResult.setPageSizeEtalon(aRes.getPageSizeEtalon());
		detailResult.setKeyWordsCount(aRes.getKeyWordsCount());
		detailResult.setLinkCount(aRes.getLinkCount());
		detailResult.setDomainNameCount(aRes.getDomainNameCount());
		detailResult.setPageUrlFinal(aRes.getPageUrlFinal());
		detailResult.setPageUrlFinalEtalon(aRes.getPageUrlFinalEtalon());
		detailResult.setStubUrl(aRes.getStubUrl());
		detailResult.setSimilarityOriginPercent(aRes.getSimilarityOriginPercent());
		detailResult.setStubScoreInfo(aRes.getStubScoreInfo());
		detailResult.setRedirectionDetected(aRes.getRedirectionDetected());
		detailResult.setResultNLP(aRes.getResultNLP());
		detailResult.setForbiddenFinalUrl(aRes.getForbiddenFinalUrl());

		detailVpnRepo.save(detailResult);

		return aRes.getCheckResult();
	}
}
