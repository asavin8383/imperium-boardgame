package common;

import lombok.extern.slf4j.Slf4j;
import model.response.DeltaAddonEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import rest.ActCheckResult;
import rest.ActRequest;
import restapi.*;
import services.ActService;
import services.MissionService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@Slf4j
public class RestTest
{
    @Autowired
    PSRestClient psRestClient;

    @Autowired
    PASDRestClient pasdRestClient;

    @Autowired
    SubTypeRestClient subTypeRestClient;

    @Autowired
    AddonRestClient addonRestClient;

    @Autowired
    ErdiRestClient erdiRestClient;

    @Autowired
    ActService actService;

    @Autowired
    MissionService missionService;

    @Test
    public void testPS(){
        psRestClient.readFromNet();

    }

    @Test
    public void testPASD(){
        pasdRestClient.readFromNet();

    }

    @Test
    public void testSybType(){
        subTypeRestClient.readFromNet();
    }

    @Test
    public void testDeltaAddonsList(){
        List<DeltaAddonEntry> list = addonRestClient.readDeltaList();
        log.debug("DeltaAddonEntry list = " + list);
    }

    @Test
    public void testAddons(){
        addonRestClient.readFullFromNet();
    }

    @Test
    public void testDeltaAddons(){
        addonRestClient.readDeltaFromNet(42, new Date());
    }

    @Test
    public void runUpdateContent(){
        erdiRestClient.startUpdateErdi();
    }

    @Test
    public void testCreateAct(){
        ActRequest actRequest = new ActRequest();
        actRequest.setArragementId(338L);
        actRequest.setStartDate("");
        actRequest.setEndDate("");

        actService.createAct(actRequest);
    }

    @Test
    public void testGetMissions(){
        missionService.fillMissionsWithConfirm(false);
    }

}
