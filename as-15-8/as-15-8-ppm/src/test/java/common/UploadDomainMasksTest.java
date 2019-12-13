package common;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import restapi.pod.DomainMaskUploader;

/**
 * Created by san
 * Date: 14.11.2019
 */
@Configuration
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "file:config/application.yml")
@Slf4j
public class UploadDomainMasksTest {

    @Autowired
    DomainMaskUploader domainMaskUploader;

    public void uploadMasks(){
        System.out.println(domainMaskUploader.getDomains("*.loudtalks.com"));
    }
}
