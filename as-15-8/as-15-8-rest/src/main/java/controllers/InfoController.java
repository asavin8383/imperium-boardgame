package controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import model.SearchSystem;
import repositories.SearchSystemRepository;

@RestController
@RequestMapping(path="/info", produces="application/json")
public class InfoController {
	
	@Autowired
	private SearchSystemRepository searchSystemRepo;

	@GetMapping("/search_systems")
	public List<SearchSystem> searchSystems(){
		return searchSystemRepo.findAll();
	}
	
	@PostMapping(path="/search_systems", consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public SearchSystem postTaco(@RequestBody SearchSystem searchSystem) {
		return searchSystemRepo.save(searchSystem);
	}
}
