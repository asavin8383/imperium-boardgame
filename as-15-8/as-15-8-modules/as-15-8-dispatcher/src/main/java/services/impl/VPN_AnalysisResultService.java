package services.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import analysis.VpnAnalysisResult;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.DetailResultsVpn;
import repositories.DetailResultsVpnRepository;
import services.AnalysisResultService;


@Slf4j
@Service
public class VPN_AnalysisResultService implements AnalysisResultService<VpnAnalysisResult> {

	private DetailResultsVpnRepository detailVpnRepo;
    private NamedParameterJdbcTemplate jdbcNamedTemplate;
    private JdbcTemplate jdbcTemplate;

	public static final String UTF8 =  StandardCharsets.UTF_8.name();

    @Autowired
    public VPN_AnalysisResultService(NamedParameterJdbcTemplate jdbcNamedTemplate,
									 JdbcTemplate jdbcTemplate,
									 DetailResultsVpnRepository detailVpnRepo) {
        this.detailVpnRepo = detailVpnRepo;
        this.jdbcNamedTemplate = jdbcNamedTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }


	@Override
	public CheckUnitJobResult processResult(VpnAnalysisResult aRes) {

		DetailResultsVpn detailResultsVpn = new DetailResultsVpn();

		detailResultsVpn.setId(aRes.getJobID());
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

		CheckUnitJobResult result = postAnalysis(detailResultsVpn, aRes);

		detailVpnRepo.save(detailResultsVpn);

		return result;
	}

	private CheckUnitJobResult postAnalysis(DetailResultsVpn detailResults, VpnAnalysisResult aRes){
		CheckUnitJobResult result = aRes.getCheckResult();

		if (aRes.getNeedTestFinalUrl() != null && aRes.getNeedTestFinalUrl()){
			if (searchCheckUnits(aRes.getPageUrlFinal())){
                result = CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED;
				detailResults.setForbiddenFinalUrl(true);
				String info = aRes.getStubScoreInfo();
				info = info == null ? "" : info + ". ";
				aRes.setStubScoreInfo(info + "Обнаружен редирект на запрещенный ресурс.");
			}
		}
		return result;
	}

	public boolean searchCheckUnits(String url){
		if (StringUtils.isEmpty(url)){
			return false;
		}

		if (!url.startsWith("http")){
			url = "http://" + url;
		}

		URL u = null;
		try {
			u = new URL(url);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}

		String host = u.getHost();
		Integer port = StringUtils.isEmpty(u.getPort()) || u.getPort() < 0 ? null : u.getPort();
		String path = StringUtils.isEmpty(u.getPath()) || u.getPath().equals("/") ? null : u.getPath();

		String h1 = java.net.IDN.toUnicode(host);
		String h2 = java.net.IDN.toASCII(host);

		String url1 = h1 +
				(port == null ? "" : ":" + port) +
				(path == null ? "" : UriUtils.decode(path, UTF8));

		String url2 = h2 +
				(port == null ? "" : ":" + port) +
				(path == null ? "" : UriUtils.encodeFragment(UriUtils.decode(path, UTF8), UTF8));

		boolean ipv4 = InetAddressValidator.getInstance().isValidInet4Address(host);
		boolean ipv6 = InetAddressValidator.getInstance().isValidInet6Address(host);
		boolean isIp = ipv4 || ipv6;
		boolean isDomain = !isIp;
		boolean isUrl = path != null;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("isDomain", isDomain);
		parameters.addValue("isV4", ipv4);
		parameters.addValue("isV6", ipv6);
		parameters.addValue("isUrl", isUrl);
		parameters.addValue("domain1", isDomain ? "%" + h1 + "%" : "");
		parameters.addValue("domain2", isDomain ? "%" + h2 + "%" : "");
        parameters.addValue("ip", isIp ? "%" + host + "%" : "");
        parameters.addValue("url1", isUrl ? "%" + url1 + "%" : "");
        parameters.addValue("url2", isUrl ? "%" + url2 + "%" : "");


        List<ResultItem> list = jdbcNamedTemplate.query(
                "select content.id as id, 'DOMAIN' as check_unit_type, domain.domain as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.domain on content.id = domain.content_id and blocktype = 'domain' \n" +
						"  where :isDomain is TRUE and ( domain.domain like :domain1 or domain.domain like :domain2 ) \n" +
                        "UNION\n" +
                        "select content.id, 'IP_V4' as check_unit_type, ip.ip as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ip on content.id = ip.content_id and blocktype = 'ip' \n" +
						"  where :isV4 is TRUE and ip = :ip \n" +
                        "UNION\n" +
                        "select content.id, 'IP_V6' as check_unit_type, ipv6.ipv6 as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipv6 on content.id = ipv6.content_id and blocktype = 'ip' \n" +
						"  where :isV6 is TRUE and ipv6 = :ip \n" +
                        "UNION\n" +
                        "select content.id, 'URL' as check_unit_type, url.url as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.url on content.id = url.content_id and blocktype is null \n " +
						"  where :isUrl is TRUE and ( url.url like :url1 or url.url like :url2 )",
                parameters,
                (rs, i) -> {
                    ResultItem resultItem = new ResultItem();
                    resultItem.setErdiId(rs.getLong("id"));
                    resultItem.setCheckUnitType(CheckUnitType.valueOf(rs.getString("check_unit_type")));
                    resultItem.setCheckUnitValue(rs.getString("check_unit_value"));
                    return resultItem;
                });

		log.info("--------------- SEARCH CHECK UNITS -----------");
		log.info(list.toString());

		return list.size() > 0;
	}

    @Data
    private static class ResultItem {
        private Long erdiId;
        private CheckUnitType checkUnitType;
        private String checkUnitValue;
    }
}
