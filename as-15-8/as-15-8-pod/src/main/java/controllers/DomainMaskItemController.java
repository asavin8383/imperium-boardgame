package controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.DomainMaskItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.DomainMaskItemRepo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 13.11.2019
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequestMapping(path = "/domain-masks")
public class DomainMaskItemController {
    private final DomainMaskItemRepo domainMaskItemRepo;

    @GetMapping
    public Set<String> getDomainMaskItems(@RequestParam String mask){
        return domainMaskItemRepo.findAllByDomainMask(mask)
                .stream()
                .map(DomainMaskItem::getDomainMaskItem)
                .collect(Collectors.toSet());
    }

}
