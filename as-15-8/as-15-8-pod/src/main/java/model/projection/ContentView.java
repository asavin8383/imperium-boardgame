package model.projection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import model.portal.ErdiTrafficUnitJoin;
import model.portal.SearchQueryTrafficUnitJoin;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@Setter(value = AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Immutable
@Subselect(
        "with help as ( \n" +
                "    select content.id                   as content_id, \n" +
                "           max(addon.id)                as addon_id, \n" +
                "           history.content_version_id   as content_version_id, \n" +
                "           min(resources.id)            as resource_id \n" +
                "    from sor.content content \n" +
                "             join sor.content_history history \n" +
                "                  on content.id = history.content_id \n" +
                "                      and history.end_dt = to_date('30000101', 'YYYYMMDD') \n" +
                "             left join sor.addon addon \n" +
                "                       on content.id = addon.content_id and \n" +
                "                          history.addon_version_id = addon.addon_version_id \n" +
                "             join sor.content_info info \n" +
                "                  on content.id = info.content_id and \n" +
                "                     history.content_version_id = info.content_version_id \n" +
                "             left join sor.content_resources resources \n" +
                "                       on content.id = resources.content_id and \n" +
                "                          history.content_version_id = info.content_version_id and \n" +
                "                          case \n" +
                "                              when info.blocktype like 'domain%' then resources.resource_type_id = 1 \n" +
                "                              when info.blocktype = 'ip' then resource_type_id in (2, 3, 4, 5) \n" +
                "                              else resources.resource_type_id = 6 \n" +
                "                              end \n" +
                "    group by content.id, history.content_version_id \n" +
                ") select --count(help.content_id) \n" +
                "      help.content_id as content_id, \n" +
                "      res.value as resource_value, \n" +
                "      restype.dsc as resource_type, \n" +
                "      subtype.orig_id as info_type_id, \n" +
                "      subtype.registry_name as registry_name, \n" +
                "      subtype.category_name as category_name, \n" +
                "      subtype.violation_name as violation_name, \n" +
                "      decision.org as decision_org \n" +
                "from help \n" +
                "         left join sor.content_resources res on help.resource_id = res.id \n" +
                "         left join sor.resource_type restype on res.resource_type_id = restype.id \n" +
                "         left join sor.addon addon on help.addon_id = addon.id \n" +
                "         left join sor.subtype subtype on addon.info_type_id = subtype.orig_id and \n" +
                "                                          subtype.eff_dt = to_date('30000101', 'YYYYMMDD') \n" +
                "         join sor.decision decision on help.content_id = decision.content_id and \n" +
                "                                       help.content_version_id = decision.content_version_id"
)
public class ContentView {

    //    http://192.168.5.182:8090/pages/viewpage.action?pageId=3801123

    // 1 or erdiId ?
    @Id
    @Column(name = "content_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Long contentId;

    // 12
    @ToString.Include
    private String resourceValue;

    // 10
    @ToString.Include
    private String resourceType;

    // 9
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

    @OneToMany(mappedBy = "contentView")
    @JsonIgnore
    private List<ErdiTrafficUnitJoin> erdiTrafficUnits;

    @OneToMany(mappedBy = "contentView")
    @JsonIgnore
    private List<SearchQueryTrafficUnitJoin> searchQueryTrafficUnits;

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

    // 14 addon
//    private Long visitorsCntRussia;

    // 15 addon
//    private Long visitorsCntWorld;

}
