package model.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentView {

//    http://192.168.5.182:8090/pages/viewpage.action?pageId=3801123

    // 1
    // @Value("#{target.id}")
    private Long contentId;

    // 1 ?
    // @Value("#{target.erdiId}")
    private String erdiId;

    // relevant version
    // @Value("#{target.contentVersion.id}")
    @JsonIgnore
    private Long contentVersionId;

    @JsonIgnore
    private Long addonVersionId;

    // 2
    // @Value("#{target.contentInfo.includetime}")
//    @JsonFormat(timezone = "GMT+03:00")
//    private Date includeTime;

    // 6
    // @Value("#{target.contentInfo.ts}")
//    @JsonFormat(timezone = "GMT+03:00")
//    private Date contentTs;

    // 4
    // @Value("#{target.contentInfo.entryType.dsc}")
//    private String entryTypeDsc;

    // 5
    // @Value("#{target.contentInfo.blockType.dsc}")
//    private String blockTypeDsc;

    // 3
    // Отсутствие данного атрибута означает обычную срочность (0)
    // @Value("#{target.contentInfo.urgencyType.dsc}")
//    private String urgencyTypeDsc;

    // 12
    // @Value("#{target.contentResources[0].value}")
    private String resourceValue;

    // 10
    // @Value("#{target.contentResources[0].checkUnitType}")
    private String resourceType;

    // 11
    // max of selected resources dates ?
    // @Value("#{target.contentResources[0].checkUnitType}")
//    private Date resourceTs;

    // 7
    // @Value("#{target.decision.date}")
//    @JsonFormat(timezone = "GMT+03:00") // check
//    private Date decisionDate;

    // 8
    // @Value("#{target.decision.number}")
//    private String decisionNumber;

    // 9
    // @Value("#{target.decision.org}")
    private String decisionOrg;

    // 13 addon
    private String infoTypeId;

    // 14 addon
//    private Long visitorsCntRussia;

    // 15 addon
//    private Long visitorsCntWorld;


    /* Parse infoTypeId */

    // 16
    private String registryName;

    // 17
    private String categoryName;

    // 18
    private String violationName;

    public ContentView(long contentId, String erdiId,
                       Long contentVersionId,
                       Long addonVersionId) {
        this.contentId = contentId;
        this.erdiId = erdiId;
        this.contentVersionId = contentVersionId;
        this.addonVersionId = addonVersionId;
    }
}
