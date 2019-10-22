package model.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentView {

    // @Value("#{target.id}")
    private Long contentId;

    // @Value("#{target.erdiId}")
    private String erdiId;

    // relevant version
    // @Value("#{target.contentVersion.id}")
    private Long contentVersionId;

    // @Value("#{target.contentInfo.includetime}")
    @JsonFormat(timezone = "GMT+03:00")
    private Date includeTime;

    // @Value("#{target.contentInfo.ts}")
    @JsonFormat(timezone = "GMT+03:00")
    private Date ts;

    // @Value("#{target.contentInfo.entryType.dsc}")
    private String entryTypeDsc;

    // @Value("#{target.contentInfo.blockType.dsc}")
    private String blockTypeDsc;

    // Отсутствие данного атрибута означает обычную срочность (0)
    // @Value("#{target.contentInfo.urgencyType.dsc}")
    private String urgencyTypeDsc;

    // @Value("#{target.contentResources[0].value}")
    private String resourceValue;

    // @Value("#{target.contentResources[0].checkUnitType}")
    private String resourceType;

    @JsonFormat(timezone = "GMT+03:00") // check
    // @Value("#{target.decision.date}")
    private Date decisionDate;

    // @Value("#{target.decision.number}")
    private String decisionNumber;

    // @Value("#{target.decision.org}")
    private String decisionOrg;

}
