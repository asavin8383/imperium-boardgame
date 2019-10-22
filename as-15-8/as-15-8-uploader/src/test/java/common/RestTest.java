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
import restapi.AddonRestClient;
import restapi.PASDRestClient;
import restapi.PSRestClient;
import restapi.SubTypeRestClient;

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

}
