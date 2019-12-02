package services.impl;

import analysis.VpnAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.PasdDetailResult;
import model.Result;
import model.enums.CheckType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PasdDetailResultRepo;
import services.AnalysisResultService;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VpnAnalysisResultService implements AnalysisResultService<VpnAnalysisResult> {

	private final PasdDetailResultRepo pasdDetailResultRepo;

	@Override
	public CheckType getCheckType() {
		return CheckType.PASD;
	}

	@Override
	public void saveResult(Result result, VpnAnalysisResult analysisResult) {

		PasdDetailResult pasdDetailResult = pasdDetailResultRepo.findById(result.getId())
				.orElseGet(() -> new PasdDetailResult());
		BeanUtils.copyProperties(pasdDetailResult, new PasdDetailResult(), "id", "result");

		pasdDetailResult.setResult(result);

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

		pasdDetailResultRepo.save(pasdDetailResult);

	}

	@Override
	public String getErrorText(VpnAnalysisResult analysisResult) {
		return analysisResult.getStubScoreInfo();
	}
}
