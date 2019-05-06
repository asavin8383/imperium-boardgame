package controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import model.catalog.SearchSystem;
import repositories.SearchSystemRepository;

@RestController
@RequestMapping(path="/info", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
public class InfoController {
	
	@Autowired
	private SearchSystemRepository searchSystemRepo;

	@GetMapping(path="/search_systems")
	public List<SearchSystem> searchSystems(Authentication authentication){
		return searchSystemRepo.findAll();
	}
	
	@PostMapping(path="/search_systems")
	@ResponseStatus(HttpStatus.CREATED)
	public SearchSystem postTaco(@RequestBody SearchSystem searchSystem) {
		return searchSystemRepo.save(searchSystem);
	}
}
