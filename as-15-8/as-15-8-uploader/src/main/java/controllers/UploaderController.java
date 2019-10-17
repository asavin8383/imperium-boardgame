package controllers;

import common.RestApiHelper;
import model.rest.control.PodInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

/**
 */

@RestController
@RequestMapping(path = "/pod", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploaderController {

    @Autowired
    private RestApiHelper restApiHelper;


    @GetMapping("/get_date_info")
    public PodInfo erdiById(){
        return  new PodInfo(new Date(), new Date());

        //return restApiHelper.getDateInfo();
    }
}
