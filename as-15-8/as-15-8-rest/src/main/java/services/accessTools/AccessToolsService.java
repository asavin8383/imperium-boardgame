package services.accessTools;

import lombok.RequiredArgsConstructor;
import model.catalog.AccessToolsCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.AccessToolsCategoriesRepo;

import java.util.List;

/**
 * Creation date: 06.09.2019
 * Author: asavin
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AccessToolsService {

    private final AccessToolsCategoriesRepo accessToolsCategoriesRepo;

    public List<AccessToolsCategory> getCategories(){
        return accessToolsCategoriesRepo.findAll();
    }
}
