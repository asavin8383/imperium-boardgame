package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.sor.FormalErdi;
import model.sor.FormalErdiView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import repositories.ContentResourceRepository;
import repositories.FormalErdiRepository;
import repositories.helpers.FormalErdiParams;
import utils.SorUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/pod/erdi/formal", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class FormalErdiController {

    private final FormalErdiRepository formalErdiRepository;
    private final ContentResourceRepository contentResourceRepository;

    // todo service

    @GetMapping
    public Page<FormalErdi> getAllFormalErdi(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(defaultValue = "false") boolean returnAll,
                                             @RequestParam(required = false) Long erdiTrafficUnitId,
                                             @RequestParam(required = false) Long searchTrafficUnitId,
                                             @RequestParam(required = false) String query,
                                             @RequestParam(required = false) Long resourceTypeId) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        FormalErdiParams params = new FormalErdiParams(returnAll,
                erdiTrafficUnitId, searchTrafficUnitId, query, resourceTypeId);
        Page<FormalErdi> result =
                formalErdiRepository.searchFor(FormalErdi.class, params, page);

        if (returnAll) {
            if (erdiTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(formalErdiRepository
                        .belongsToErdiTrafficUnit(erdiTrafficUnitId, rec.getId())));
            else if (searchTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(formalErdiRepository
                        .belongsToSearchTrafficUnit(searchTrafficUnitId, rec.getId())));
        }
        return result;
    }

    @GetMapping(path = "/dict")
    public Page<FormalErdiView> getAllFormalErdiDict(@RequestParam(required = false) SortingDirection sortingDirection,
                                                     @RequestParam(required = false) String sortingColumn,
                                                     @RequestParam(defaultValue = "0") int pageNumber,
                                                     @RequestParam(defaultValue = "10") int pageSize) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        Page<FormalErdi> erdiPage = formalErdiRepository.findRelevant(
                null, null, null, null, null, page);

        List<FormalErdiView> viewList = erdiPage.getContent().stream()
                .map(erdi -> {
                    FormalErdiView view = new FormalErdiView();
                    view.setId(erdi.getId());
                    view.setErdiId(erdi.getErdiId());
                    view.setInitContentVersionId(erdi.getInitContentVersionId());
                    view.setContentInfo(getFirst(erdi.getContentInfo()));

                    String blockType = view.getContentInfo().getBlockType();
                    String resourceTypeLike = SorUtils.getResourceTypeLike(blockType);
                    view.setExampleResource(contentResourceRepository
                            .findOneByDescription(resourceTypeLike));
                    return view;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(viewList, page, erdiPage.getTotalElements());
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public FormalErdi getFormalErdiById(@PathVariable Long id) {
        return formalErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Formal ERDI was not found by id: " + id));
    }

    @Nullable
    private static <T> T getFirst(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }
}
