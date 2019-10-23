package services.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import model.traffic.CustomErdi;
import model.traffic.projection.CustomErdiRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import repositories.CustomErdiRepository;
import repositories.helpers.CustomErdiParams;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CustomErdiService {

    private final CustomErdiRepository customErdiRepository;

    public Page<CustomErdi> getAllCustomErdi(SortingDirection sortingDirection,
                                             String sortingColumn,
                                             int pageNumber,
                                             int pageSize,
                                             boolean returnAll,
                                             Long erdiTrafficUnitId,
                                             Long searchTrafficUnitId,
                                             String query,
                                             //Long resourceTypeId,
                                             Long violationId) {
        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        CustomErdiParams params = new CustomErdiParams(returnAll,
                erdiTrafficUnitId, searchTrafficUnitId, query, violationId);
        Page<CustomErdi> result =
                customErdiRepository.searchFor(CustomErdi.class, params, page);

        if (returnAll) {
            if (erdiTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(customErdiRepository
                        .belongsToErdiTrafficUnit(erdiTrafficUnitId, rec.getId())));
            else if (searchTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(customErdiRepository
                        .belongsToSearchTrafficUnit(searchTrafficUnitId, rec.getId())));
        }
        return result;
    }

    public Page<CustomErdiRow> getCustomErdiRows(SortingDirection sortingDirection,
                                                 String sortingColumn,
                                                 int pageNumber,
                                                 int pageSize,
                                                 boolean returnAll,
                                                 Long erdiTrafficUnitId,
                                                 Long searchTrafficUnitId,
                                                 String query,
                                                 Long violationId) {
        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        CustomErdiParams params = new CustomErdiParams(returnAll,
                erdiTrafficUnitId, searchTrafficUnitId, query, violationId);
        Page<CustomErdiRow> result = customErdiRepository.searchFor(params, page);

        if (returnAll) {
            if (erdiTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(customErdiRepository
                        .belongsToErdiTrafficUnit(erdiTrafficUnitId, rec.getCustomErdiId())));
            else if (searchTrafficUnitId != null)
                result.forEach(rec -> rec.setChecked(customErdiRepository
                        .belongsToSearchTrafficUnit(searchTrafficUnitId, rec.getCustomErdiId())));
        }
        return result;
    }

    public CustomErdi createCustomErdi(CustomErdi customErdi) {
        return customErdiRepository.save(customErdi);
    }

    public CustomErdi getCustomErdiById(Long id) {
        return customErdiRepository.findById(id).orElseThrow(() ->
                new AS_15_8_Exception("Custom ERDI was not found by id: " + id));
    }

    public CustomErdi updateCustomErdi(CustomErdi newCustomErdi,
                                       CustomErdi customErdi) {
        customErdi.setName(newCustomErdi.getName());
        customErdi.setViolation(newCustomErdi.getViolation());
        //customErdi.setCustomErdiUnits(newCustomErdi.getCustomErdiUnits());
        return customErdiRepository.save(customErdi);
    }

    public void deleteCustomErdi(Long id) {
        customErdiRepository.deleteById(id);
    }
}
