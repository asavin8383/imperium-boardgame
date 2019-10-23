package controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

import repositories.AccessToolRepository;

@RestController
@RequestMapping(path="/info", produces=MediaType.APPLICATION_JSON_VALUE)
public class InfoController {
	
	@Autowired
	private AccessToolRepository searchSystemRepo;

	@GetMapping(path="/access_tools")
	public CompletionStage<List<AccessTool>> accessToolsList(Authentication authentication){
		return CompletableFuture.supplyAsync(searchSystemRepo::findAll);
	}
	
	@PostMapping(path="/access_tools", consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public AccessTool postAccessTool(@RequestBody AccessTool accessTool) {
		return searchSystemRepo.save(accessTool);
	}
}
