package model;

import controllers.utils.SortingDirection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Data
@Slf4j

public class ErdiFilterFields {

    private String idMask;
    private List<String> categoryNames;
    private List<String> decisionOrgs;
    private List<String> infoTypeIds;
    private List<String> registryNames;
    private List<String> resourceTypes;
    private String resourceValue;
    private List<String> violationNames;
    private Integer pageSize;
    private LocalDate startTime;
    private LocalDate endTime;
    private Boolean random;
    private SortingDirection sortingDirection;
    private String sortingColumn;
    private Long visitorsCntRussiaMin;
    private Long visitorsCntRussiaMax;
    private Long visitorsCntWorldMin;
    private Long visitorsCntWorldMax;
    private Integer pageNumber;
    
    public static ErdiFilterFields loadErdiFilterFields(String query) {
        
        ErdiFilterFields eff = new ErdiFilterFields();
        
        List<NameValuePair> nameValuePairs = parseParams(query);

        nameValuePairs.forEach(nameValuePair -> {
            try {
                Class<?> fieldType = ErdiFilterFields.class.getDeclaredField(nameValuePair.getName()).getType();
                String setMethodName = "set" + nameValuePair.getName().substring(0,1).toUpperCase() + nameValuePair.getName().substring(1);
                Method setMethod = ErdiFilterFields.class.getMethod(setMethodName, fieldType);
                Object value = null;
                
                if (fieldType.equals(Long.class)) {
                    value = Long.parseLong(nameValuePair.getValue());
                } else if (fieldType.equals(List.class)) {
                    value = Arrays.asList(nameValuePair.getValue().split(","));
                } else if (fieldType.equals(Integer.class)) {
                    value = Integer.parseInt(nameValuePair.getValue());
                } else if (fieldType.equals(LocalDate.class)) {
                    value = DateTimeFormatter.ISO_DATE.parse(nameValuePair.getValue());
                } else if (fieldType.equals(Boolean.class)) {
                    value = Boolean.parseBoolean(nameValuePair.getValue());
                } else if (fieldType.equals(String.class)) {
                    value = nameValuePair.getValue();
                } else if (fieldType.equals(SortingDirection.class)) {
                    value = SortingDirection.valueOf(nameValuePair.getValue().toUpperCase());
                }  else {
                    log.warn("Тип поля не поддерживается в ErdiFilterFields: " + nameValuePair.getName());
                }
                
                if (value != null) {
                    setMethod.invoke(eff, value);
                }
                
            } catch (Exception ex) {
                log.warn("Поле " + nameValuePair.getName() + " не определено в ErdiFilterFields ", ex);
            }
        });
        return eff;
    }

    private static List<NameValuePair> parseParams(String query) {
        List<NameValuePair> params =
                URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        return params;
    }
    
}
