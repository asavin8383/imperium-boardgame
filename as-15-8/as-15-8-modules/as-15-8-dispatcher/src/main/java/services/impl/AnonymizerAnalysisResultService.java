package services.impl;

import analysis.AnonymizerAnalysisResult;
import enums.CheckUnitJobResult;
import model.DetailResultsVpn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.DetailResultsVpnRepository;
import services.AnalysisResultService;


@Service
public class AnonymizerAnalysisResultService implements AnalysisResultService<AnonymizerAnalysisResult> {

	@Autowired
	DetailResultsVpnRepository detailVpnRepo;

	@Override
	public CheckUnitJobResult processResult(AnonymizerAnalysisResult aRes) {

		DetailResultsVpn detailResultsVpn = new DetailResultsVpn();

		detailResultsVpn.setId(aRes.getJobID());
		detailResultsVpn.setResponseErrorCode(aRes.getErrorCode());
		detailResultsVpn.setPageSize(aRes.getPageSize());
		detailResultsVpn.setResponseErrorCodeEtalon(aRes.getEtalonErrorCode());
		detailResultsVpn.setPageSizeEtalon(aRes.getEtalonPageSize());
		detailResultsVpn.setPageUrlFinal(aRes.getFinalUrl());
		detailResultsVpn.setStubUrl(aRes.getStubUrl());
		detailResultsVpn.setKeyWordsCount(aRes.getKeyWordsCount());
		detailResultsVpn.setLinkCount(aRes.getLinkCount());
		detailResultsVpn.setDomainNameCount(aRes.getDomainNameCount());
		detailResultsVpn.setStubScoreInfo(aRes.getStubScoreInfo());
		detailResultsVpn.setSimilarityOriginPercent(aRes.getSimilarityPercent());

		detailResultsVpn.setResponseError(aRes.getErrorCode() != null);
		detailVpnRepo.save(detailResultsVpn);

		return aRes.getCheckResult();
	}
}
