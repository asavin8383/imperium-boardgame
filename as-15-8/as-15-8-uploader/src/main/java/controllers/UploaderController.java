package controllers;

import common.RestApiHelper;
import model.rest.control.PodState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;



@RestController
@RequestMapping(path = "/pod", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploaderController {

    @Autowired
    private RestApiHelper restApiHelper;

    @GetMapping("/update_erdi")
    public void updateErdi() throws ParseException {
        restApiHelper.startUpdateErdi();
    }

    @GetMapping("/get_state")
    public PodState getState() throws ParseException {
        return restApiHelper.getLoadState();
    }

}
