package services.impl;

import analysis.VpnAnalysisResult;
import enums.CheckUnitJobResult;
import model.DetailResultsVpn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import repositories.DetailResultsVpnRepository;
import services.AnalysisResultService;

import java.net.URI;
import java.net.URISyntaxException;


@Service
public class VPN_AnalysisResultService implements AnalysisResultService<VpnAnalysisResult> {

	@Autowired
	DetailResultsVpnRepository detailVpnRepo;

	@Override
	public CheckUnitJobResult processResult(VpnAnalysisResult aRes) {

		DetailResultsVpn detailResultsVpn = new DetailResultsVpn();
		CheckUnitJobResult result = aRes.getCheckResult();

		detailResultsVpn.setId(aRes.getJobID());
		detailResultsVpn.setResponseErrorCode(aRes.getResponseErrorCode());
		detailResultsVpn.setResponseErrorCodeEtalon(aRes.getResponseErrorCodeEtalon());
		detailResultsVpn.setResponseError(aRes.getResponseError());
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

		if (aRes.getNeedTestFinalUrl()){
			if (searchUrlInErdi(aRes.getPageUrlFinal())){
				result = CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED;
				detailResultsVpn.setForbiddenFinalUrl(true);
			}
		}

		detailVpnRepo.save(detailResultsVpn);

		return result;
	}

	@SuppressWarnings("unused")
	private Boolean searchUrlInErdi(String url){
		if (StringUtils.isEmpty(url)){
			return false;
		}

		URI u = null;
		try {
			u = new URI(url);
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}

		String host = u.getHost();
		String h1 = java.net.IDN.toUnicode(host);
		String h2 = java.net.IDN.toASCII(host);

		return false;
	}
}
