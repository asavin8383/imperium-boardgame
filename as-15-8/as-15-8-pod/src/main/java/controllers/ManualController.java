package controllers;

import lombok.extern.slf4j.Slf4j;
import model.response.PASDEntry;
import model.response.PSEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import updaters.PASDDictionaryUpdater;
import updaters.PSDictionaryUpdater;

@RestController
@RequestMapping(path = "/manual", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@Slf4j
public class ManualController
{
    private final JdbcTemplate jdbcTemplate;

    private final PSDictionaryUpdater psDictionaryUpdater;
    private final PASDDictionaryUpdater pasdDictionaryUpdater;

    @Autowired
    public ManualController(JdbcTemplate jdbcTemplate, PSDictionaryUpdater psDictionaryUpdater, PASDDictionaryUpdater pasdDictionaryUpdater) {
        this.jdbcTemplate = jdbcTemplate;
        this.psDictionaryUpdater = psDictionaryUpdater;
        this.pasdDictionaryUpdater = pasdDictionaryUpdater;
    }

    @PostMapping("ps")
    public void postPS(@RequestBody PSEntry entry) {
        if (entry.getId() == null) entry.setId(getOrigId());
        psDictionaryUpdater.insertRecord(entry);
    }

    @PostMapping("pasd")
    public void postPASD(@RequestBody PASDEntry entry) {
        if (entry.getId() == null ) entry.setId(getOrigId());
        pasdDictionaryUpdater.insertRecord(entry);
    }

    private long getOrigId() {
        //noinspection ConstantConditions
        return -1 * jdbcTemplate.queryForObject("select nextval('sor.orig_id_seq')", Long.class);
    }


}
