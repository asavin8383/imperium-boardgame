package model.projection;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Setter(value = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
@Table(schema = "sor", name = "content_view")
public class ContentView {

    //    http://192.168.5.182:8090/pages/viewpage.action?pageId=3801123

    // erdiId
    @Id
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "includetime")
    @ToString.Include
    private LocalDateTime includetime;

    // 10
    @ToString.Include
    private String resourceType;

    // 9id
    @ToString.Include
    private String decisionOrg;

    // 13
    @ToString.Include
    private String infoTypeId;

    // 16
    private String registryName;

    // 17
    private String categoryName;

    // 18
    private String violationName;

    @ToString.Include
    private Long visitorsCntRussia;

    @ToString.Include
    private Long visitorsCntWorld;

    @ToString.Include
    private Long contentId;
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


}
