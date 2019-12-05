package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.catalog.AccessToolsCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.accessTools.AccessToolsService;

import java.util.List;

/**
 * Creation date: 06.09.2019
 * Author: asavin
 */
@RestController
@RequestMapping(path = "/access_tools_categories", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_PREPARATION_TRAFFIC')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class AccessToolsCategoriesController {

    private final AccessToolsService accessToolsService;

    @GetMapping
    public List<AccessToolsCategory> getAll(){
        return accessToolsService.getCategories();
    }
}
