package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ErrorDetailResult;
import model.NmapDetailResult;
import model.PasdDetailResult;
import model.PsDetailResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ErrorDetailResultRepo;
import repositories.NmapDetailResultRepo;
import repositories.PasdDetailResultRepo;
import repositories.PsDetailResultRepo;

@RestController
@RequestMapping(path = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class DetailsController {

    private final PasdDetailResultRepo pasdDetailResultRepo;
    private final PsDetailResultRepo psDetailResultRepo;
    private final NmapDetailResultRepo nmapDetailResultRepo;
    private final ErrorDetailResultRepo errorDetailResultRepo;

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @GetMapping("/pasd")
    public ResponseEntity<PasdDetailResult> getPasdDetails(@RequestParam Long id){
        return pasdDetailResultRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @GetMapping(path = "/ps")
    public ResponseEntity<PsDetailResult> getPsDetails(@RequestParam Long id) {
        return psDetailResultRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @GetMapping(path = "/nmap")
    public ResponseEntity<NmapDetailResult> getNmapDetails(@RequestParam Long id) {
        return nmapDetailResultRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasRole('ROLE_OPERATOR')")
    @GetMapping(path = "/error")
    public ResponseEntity<ErrorDetailResult> getErrorDetails(@RequestParam Long id) {
        return errorDetailResultRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
