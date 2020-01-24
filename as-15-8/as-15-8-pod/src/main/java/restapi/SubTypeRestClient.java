package restapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import model.response.RestResponseSubTypeList;
import model.rest.SubType;
import model.scheme.Subtype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import repositories.SubtypeRepository;
import updaters.SubTypeDictionaryUpdater;

import java.util.*;
import java.util.stream.Collectors;

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
    RestTemplate registryAnonimyzersRestTemplate;

    @Autowired
    SubtypeRepository subtypeRepository;

    @Value("${spring.rest_base_url}")
    private String baseUrl;


    /**
     * @return Результат true, в случае, если после загрузки есть изменения в БД. false - изменений нет.
     */
    public boolean readFromNetDiff() {
        List<Subtype> list1 = subtypeRepository.findAll();
        readFromNet();
        List<Subtype> list2 = subtypeRepository.findAll();

        return !compareSubtypeLists(list1, list2);
    }

    private boolean compareSubtypeLists(List<Subtype> list1, List<Subtype> list2){
        list1 = list1.stream()
                .filter(subtype -> subtype.getEffDt().after((new GregorianCalendar(2999, Calendar.JANUARY, 0)).getTime()))
                .collect(Collectors.toList());
        list2 = list2.stream()
                .filter(subtype -> subtype.getEffDt().after((new GregorianCalendar(2999, Calendar.JANUARY, 0)).getTime()))
                .collect(Collectors.toList());

        if (list1.size() != list2.size())
            return false;

        Map<String, Subtype> map1 = list1.stream().collect(Collectors.toMap(Subtype::getOrigId, subtype -> subtype));
        Map<String, Subtype> map2 = list2.stream().collect(Collectors.toMap(Subtype::getOrigId, subtype -> subtype));

        boolean equals = true;
        for(String k1 : map1.keySet()){
            Subtype s1 = map1.get(k1);
            Subtype s2 = map2.get(k1);

            if (s2 == null ||
                    !compareStrings(s1.getRegistryName(), s2.getRegistryName()) ||
                    !compareStrings(s1.getCategoryName(), s2.getCategoryName()) ||
                    !compareStrings(s1.getViolationName(), s2.getViolationName())
            ){
                equals = false;
                break;
            }
        }
        return equals;
    }

    private boolean compareStrings(String str1, String str2){
        return (str1 == null && str2 == null) || (str1 != null && str1.equals(str2));
    }

    public void readFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getSubTypeList/";
        log.info("GET from {}", url);
        ResponseEntity<RestResponseSubTypeList> entity = registryAnonimyzersRestTemplate.getForEntity(url, RestResponseSubTypeList.class);

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

        //System.out.println("subTypeEntries = " + subTypeEntries);

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
