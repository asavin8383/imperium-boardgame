package common;

import model.DetailResult;
import model.PasdDetailResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import services.impl.*;

import javax.persistence.EntityManager;

/**
 * Created by san
 * Date: 03.03.2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class UpsertTest {

    @Autowired
    private VpnDetailResultService vpnDetailResultService;
    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    public void testUpsert(){
        DetailResult detailResult = new PasdDetailResult();
        detailResult.setId(10L);
        vpnDetailResultService.save(entityManager, detailResult);
    }
}
