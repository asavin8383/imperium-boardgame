package services.impl;

import analysis.VpnAnalysisResult;
import enums.CheckUnitJobResult;
import lombok.extern.slf4j.Slf4j;
import model.DetailResultsVpn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.DetailResultsVpnRepository;
import services.AnalysisResultService;


@Slf4j
@Service
public class VPN_AnalysisResultService implements AnalysisResultService<VpnAnalysisResult> {

	private DetailResultsVpnRepository detailVpnRepo;

    @Autowired
    public VPN_AnalysisResultService(DetailResultsVpnRepository detailVpnRepo) {
        this.detailVpnRepo = detailVpnRepo;
    }

	@Override
	public CheckUnitJobResult processResult(VpnAnalysisResult aRes) {

		DetailResultsVpn detailResultsVpn = new DetailResultsVpn();

		detailResultsVpn.setId(aRes.getJobID());
		detailResultsVpn.setHttpStatus(aRes.getHttpStatus());
		detailResultsVpn.setHttpStatusEtalon(aRes.getHttpStatusEtalon());
		detailResultsVpn.setHttpHeaders(aRes.getHttpHeaders());
		detailResultsVpn.setHttpHeadersEtalon(aRes.getHttpHeadersEtalon());
		detailResultsVpn.setResponseErrorCode(aRes.getResponseErrorCode());
		detailResultsVpn.setResponseErrorCodeEtalon(aRes.getResponseErrorCodeEtalon());
		detailResultsVpn.setResponseError(aRes.getResponseError());
		detailResultsVpn.setUseEtalon(aRes.getUseEtalon());
		detailResultsVpn.setPageSize(aRes.getPageSize());
		detailResultsVpn.setPageSizeEtalon(aRes.getPageSizeEtalon());
		detailResultsVpn.setKeyWordsCount(aRes.getKeyWordsCount());
		detailResultsVpn.setLinkCount(aRes.getLinkCount());
		detailResultsVpn.setDomainNameCount(aRes.getDomainNameCount());
		detailResultsVpn.setPageUrlFinal(aRes.getPageUrlFinal());
		detailResultsVpn.setPageUrlFinalEtalon(aRes.getPageUrlFinalEtalon());
		detailResultsVpn.setStubUrl(aRes.getStubUrl());
		detailResultsVpn.setSimilarityOriginPercent(aRes.getSimilarityOriginPercent());
		detailResultsVpn.setStubScoreInfo(aRes.getStubScoreInfo());
		detailResultsVpn.setRedirectionDetected(aRes.getRedirectionDetected());
		detailResultsVpn.setResultNLP(aRes.getResultNLP());
		detailResultsVpn.setForbiddenFinalUrl(aRes.getForbiddenFinalUrl());

		detailVpnRepo.save(detailResultsVpn);

		return aRes.getCheckResult();
	}
}
