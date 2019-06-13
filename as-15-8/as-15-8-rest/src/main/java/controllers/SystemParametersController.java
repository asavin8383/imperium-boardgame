package controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import security.JWTLoginFilter;

@RestController
@RequestMapping(path = "/system_parameters", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j
public class SystemParametersController {

    private JdbcTemplate jdbcTemplate;
    private JWTLoginFilter jwtLoginFilter;

    @Autowired
    public SystemParametersController(JdbcTemplate jdbcTemplate, JWTLoginFilter jwtLoginFilter) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtLoginFilter = jwtLoginFilter;
    }

    @PutMapping(path = "/ttl")
    public ResponseEntity<Long> updateTTL(@RequestParam Long ttl){
        if(ttl <= 0){
            log.error("Error updating ttl. TTL must be greater than 0. Received: " + ttl);
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
        }
        int rowsAffected = jdbcTemplate.update("update system.system_parameters set value = ? where key = 'jwt_ttl_sec'", ttl.toString());
        if (rowsAffected==1){
            jwtLoginFilter.setTtl_msec(ttl*1000);
            return new ResponseEntity<>(ttl, HttpStatus.OK);
        } else {
            log.error("Error updating ttl. Number of affected rows was not expected: " + rowsAffected);
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
