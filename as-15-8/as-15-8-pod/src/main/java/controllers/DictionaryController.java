package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import enums.Dictionary;
import model.projection.DictionaryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.DictionaryService;

import java.util.List;

@RestController
@RequestMapping(path = "/dict", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_MANAGE_DICT')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping
    public List<DictionaryView> getDictionaryViewList() {
        return dictionaryService.getDictionaryViewList();
    }

    // to do js - converter

    @GetMapping(path = "/{code}")
    public DictionaryView getDictionaryView(@PathVariable("code") Dictionary dictionary) {
        return dictionaryService.getDictionaryView(dictionary);
    }

}
