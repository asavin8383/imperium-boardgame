package restapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.response.RestResponseSubTypeList;
import model.rest.SubType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import restapi.updaters.SubTypeDictionaryUpdater;

import java.util.*;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:43
 */
@Component
@Slf4j
public class SubTypeRestClient
{
    @Autowired
    SubTypeDictionaryUpdater subTypeDictionaryUpdater;

    @Autowired
    RestTemplate restTemplate;

    @Value("${spring.rest_base_url}")
    private String baseUrl;


    public void readFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getSubTypeList/";
        log.info("GET from {}", url);
        ResponseEntity<RestResponseSubTypeList> entity = restTemplate.getForEntity(url, RestResponseSubTypeList.class);

        RestResponseSubTypeList resp = entity.getBody();
        System.out.println("resp = " + resp);

        List<SubTypeEntry> subTypeEntries = new ArrayList<>();
        Map<String, SubType> map = resp.subTypeMap();
        for (SubType subType : map.values()) {
            String[] id_parts = subType.getId().split("-");
            switch (id_parts.length) {
                case 1:
                    subTypeEntries.add(new SubTypeEntry(
                            subType.getId(),
                            resp.getDate(),
                            subType.getName(),
                            null,
                            null
                    ));
                    break;
                case 2:
                    subTypeEntries.add(new SubTypeEntry(
                            subType.getId(),
                            resp.getDate(),
                            map.get(id_parts[0]).getName(),
                            subType.getName(),
                            null
                    ));
                    break;
                case 3:
                    subTypeEntries.add(new SubTypeEntry(
                            subType.getId(),
                            resp.getDate(),
                            map.get(id_parts[0]).getName(),
                            map.get(id_parts[0]+"-"+id_parts[1]).getName(),
                            subType.getName()
                    ));
                    break;
            }
        }

        System.out.println("subTypeEntries = " + subTypeEntries);

        log.info("Got PS records: {}", subTypeEntries.toString());

        subTypeDictionaryUpdater.insertRecords(subTypeEntries);
    }

    @Data
    @AllArgsConstructor
    public class SubTypeEntry
    {
        String id;
        Date date;
        String registry_name;
        String category_name;
        String violation_name;
    }
}
