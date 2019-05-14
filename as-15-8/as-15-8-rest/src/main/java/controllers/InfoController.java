package controllers;

import java.util.List;

import model.catalog.AccessTool;
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

import repositories.SearchSystemRepository;

@RestController
@RequestMapping(path="/info", produces=MediaType.APPLICATION_JSON_VALUE)
public class InfoController {
	
	@Autowired
	private SearchSystemRepository searchSystemRepo;

	@GetMapping(path="/search_systems")
	public List<AccessTool> searchSystems(Authentication authentication){
		return searchSystemRepo.findAll();
	}
	
	@PostMapping(path="/search_systems", consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public AccessTool postTaco(@RequestBody AccessTool accessTool) {
		return searchSystemRepo.save(accessTool);
	}
}
