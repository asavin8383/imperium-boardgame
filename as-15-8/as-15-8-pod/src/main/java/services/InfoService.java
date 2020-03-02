package services;

import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.controller.SearchErdiItem;
import model.converters.ResourceTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import rest.ResponseStatusString;
import utils.URLComponent;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class InfoService {

    private final NamedParameterJdbcTemplate jdbcNamedTemplate;


    public ResponseStatusString searchCheckUnit(String url){
        ResponseStatusString result = new ResponseStatusString();
        result.setStatus(false);

        URLComponent comp = null;
        try {
            URLComponent.testUrl(url, true);
            comp = URLComponent.fromString(url);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }

        List<List<SearchErdiItem>> lists = new ArrayList<>();

        String sql_ip = "select \n" +
                "T.erdi_id, \n" +
                "RS.content_id, \n" +
                "RS.resource_type_id, \n" +
                "RS.value " +
                "from \n" +
                "sor.content T \n" +
                "join sor.content_history H on T.id = H.content_id \n" +
                "join sor.content_resources RS \n" +
                "on H.content_id = RS.content_id  and H.end_dt = to_date('3000-01-01','yyyy-mm-dd') \n" +
                "where RS.resource_type_id in (:ids) and value in (:vals)";

        if (comp.isIp()){
            MapSqlParameterSource parameters = new MapSqlParameterSource();

            List<Integer> ids = ResourceTypeConverter.typeToIntList(CheckUnitType.IP_V4, CheckUnitType.IP_V6);
            parameters.addValue("ids", ids);
            parameters.addValue("vals", Arrays.asList(comp.getHost()));
            lists.add(getResults(sql_ip, parameters));
        }

        if (comp.isDomain()){
            MapSqlParameterSource parameters = new MapSqlParameterSource();

            List<Integer> ids = new ArrayList<>();
            ids.add(ResourceTypeConverter.typeToInt(CheckUnitType.DOMAIN));
            if (comp.isDomainMask()){
                ids.add(ResourceTypeConverter.typeToInt(CheckUnitType.DOMAIN_MASK));
            }

            parameters.addValue("ids", ids);
            parameters.addValue("vals", getHosts(comp));
            lists.add(getResults(sql_ip, parameters));
        }

        if (comp.isUrl()){
            MapSqlParameterSource parameters = new MapSqlParameterSource();

            List<Integer> ids = ResourceTypeConverter.typeToIntList(CheckUnitType.URL);
            parameters.addValue("ids", ids);
            parameters.addValue("vals", getUrls(comp));
            lists.add(getResults(sql_ip, parameters));
        }

        List<SearchErdiItem> all = new ArrayList<>();
        for (List<SearchErdiItem> list : lists){
            all.addAll(list);
        }
        Set<String> setErdi = all.stream().map(item -> {return ""+item.getErdiOriginId();}).collect(Collectors.toSet());

        log.info("Найденые ЕРДИ ({}): {}, для URL: {}", setErdi.size(), setErdi.toString(), url);

        result.setStatus(setErdi.size() > 0);
        result.setResponse(setErdi.size() == 0 ? "" : ""+ Arrays.asList(setErdi).get(0));
        return result;
    }

    private List<SearchErdiItem> getResults(String sql, MapSqlParameterSource parameters){
        return jdbcNamedTemplate.query(sql,
                parameters,
                (rs, i) -> {
                    Integer typeId = rs.getInt("resource_type_id");
                    CheckUnitType checkUnitType = ResourceTypeConverter.intToType(typeId);

                    SearchErdiItem item = new SearchErdiItem();
                    item.setErdiOriginId(rs.getLong("erdi_id"));
                    item.setErdiId(rs.getLong("content_id"));
                    item.setCheckUnitType(checkUnitType);
                    item.setCheckUnitValue(rs.getString("value"));
                    return item;
                });
    }

    private Set<String> getHosts(URLComponent comp){
        Set<String> set = new HashSet<>();
        set.add(comp.getHost());

        try {
            URLComponent decoded = URLComponent.getDecodedFrom(comp);
            set.add(decoded.getHost());
        }
        catch (Exception e){}

        try {
            URLComponent encoded = URLComponent.getEncodedFrom(comp);
            set.add(encoded.getHost());
        }
        catch (Exception e){}

        return set;
    }

    private Set<String> getUrls(URLComponent comp){
        Set<String> set = new HashSet<>();
        set.add(comp.toString());

        try {
            URLComponent decoded = URLComponent.getDecodedFrom(comp);
            set.add(decoded.toString());
        }
        catch (Exception e){}

        try {
            URLComponent encoded = URLComponent.getEncodedFrom(comp);
            set.add(encoded.toString());
        }
        catch (Exception e){}

        return set;
    }

}