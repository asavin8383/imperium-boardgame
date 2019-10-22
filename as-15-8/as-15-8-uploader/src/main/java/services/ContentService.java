package services;

import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import model.enums.BlockType;
import model.projection.ContentView;
import model.scheme.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentInfoRepository infoRepository;
    private final ContentResourcesRepository resourceRepository;
    private final AddonRepository addonRepository;
    private final SubtypeRepository subtypeRepository;
    private final DecisionRepository decisionRepository;

    @Transactional
    public Page<ContentView> getByEffDtAndQuery(Date effDt, String query, Pageable page) {
        Page<ContentView> contentPage = contentRepository.findByEffDtAndQuery(effDt, query, page);

        contentPage.getContent().parallelStream().forEach(view -> {
            Decision decision = decisionRepository.findByContent_IdAndContentVersion_Id(
                    view.getContentId(), view.getContentVersionId());
            ContentInfo info = infoRepository.findOneByContentVersion_IdAndContent_Id(
                    view.getContentVersionId(), view.getContentId());
            List<String> resourceTypes = getResourceTypesFor(info.getBlockType());
            ContentResources res = resourceRepository.findOneByContentAndVersionAndTypeDsc(
                    view.getContentId(), view.getContentVersionId(), resourceTypes);

            view.setDecisionOrg(decision.getOrg());
            view.setResourceValue(res.getValue());
            view.setResourceType(res.getCheckUnitType().toString());

            Addon addon = addonRepository.findTopByAddonVersion_IdOrderByIdDesc(view.getAddonVersionId());
            if (addon != null && addon.getInfoTypeId() != null) {
                Subtype subtype = subtypeRepository.findByOrigId(addon.getInfoTypeId());
                view.setInfoTypeId(subtype.getOrigId());
                view.setRegistryName(subtype.getRegistryName());
                view.setCategoryName(subtype.getCategoryName());
                view.setViolationName(subtype.getViolationName());
            }
        });

        return contentPage;
    }

    public static List<String> getResourceTypesFor(BlockType blockType) {
        // todo bi map
        switch (blockType) {
            case IP:
                return Arrays
                        .stream(CheckUnitType.values())
                        .map(CheckUnitType::toString)
                        .filter(type -> type.contains("ip"))
                        .collect(Collectors.toList());
            case DOMAIN:
            case DOMAIN_MASK:
                return Collections.singletonList(CheckUnitType.DOMAIN.toString().toLowerCase());
            default:
                return Collections.singletonList(CheckUnitType.URL.toString().toLowerCase());
        }
    }

}
