package services;

import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.controller.SearchErdiItem;
import model.controller.SearchErdiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import rest.ResponseStatusString;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class InfoService {

    private final NamedParameterJdbcTemplate jdbcNamedTemplate;
    public static final String UTF8 = StandardCharsets.UTF_8.name();


    public ResponseStatusString searchCheckUnit(String url){
        ResponseStatusString result = new ResponseStatusString();
        result.setStatus(false);

        if (StringUtils.isEmpty(url)){
            return result;
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
            return result;
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

        String sql = "select RS.content_id as id, \n" +
                "CASE \n" +
                "  WHEN (RT.id = 1) THEN 'DOMAIN'\n" +
                "  WHEN (RT.id = 2) THEN 'IP_V4'\n" +
                "  WHEN (RT.id = 3) THEN 'IP_V6'\n" +
                "  WHEN (RT.id = 6) THEN 'URL'\n" +
                " END as check_unit_type,\n" +
                "RS.value as check_unit_value \n" +
                "from \n" +
                "sor.content_resources RS join sor.resource_type RT on RS.resource_type_id = RT.id\n" +
                "where \n" +
                "(:isDomain is TRUE and RT.id = 1 and (RS.value like :domain1 or RS.value like :domain2)) or \n" +
                "(:isV4 is TRUE and RT.id = 2 and (RS.value = :ip)) or \n" +
                "(:isV6 is TRUE and RT.id = 3 and (RS.value = :ip)) or \n" +
                "(:isUrl is TRUE and RT.id = 6 and (RS.value like :url1 or RS.value like :url2))";

        List<SearchErdiItem> list = jdbcNamedTemplate.query(sql,
                parameters,
                (rs, i) -> {
                    SearchErdiItem item = new SearchErdiItem();
                    item.setErdiId(rs.getLong("id"));
                    item.setCheckUnitType(CheckUnitType.valueOf(rs.getString("check_unit_type")));
                    item.setCheckUnitValue(rs.getString("check_unit_value"));
                    return item;
                });

        log.info("Result on search check unit: " + (list.size() > 0) + ", for URL: " + url);
        //log.info(list.toString());

        result.setStatus(list.size() > 0);
        result.setResponse(list.size() == 0 ? "" : ""+list.get(0).getErdiId());
        return result;
    }

}
