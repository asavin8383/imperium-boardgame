package controllers;

import common.RestApiHelper;
import model.enums.ParamSor;
import model.rest.control.PodState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import repositories.ParameterRepository;
import repositories.impl.ParameterRepositoryExtend;

import java.text.ParseException;
import java.util.Date;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UploaderController {

    @Autowired
    private RestApiHelper restApiHelper;

    @Autowired
    private ParameterRepositoryExtend parameterRepository;

    @GetMapping("/update_erdi")
    public void updateErdi() throws ParseException {
        restApiHelper.startUpdateErdi();
        //restApiHelper.getDumpDeltaListByDate(new Date());
    }

    @GetMapping("/remove_last_content_version")
    public void removeLastContentVersion() throws ParseException {
        restApiHelper.removeLastContentVersion();
    }

    @GetMapping("/get_state")
    public PodState getState() throws ParseException {
        return restApiHelper.getLoadState();
    }

}
