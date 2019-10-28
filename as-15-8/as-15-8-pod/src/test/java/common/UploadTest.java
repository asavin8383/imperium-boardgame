package common;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import restapi.PASDUploader;
import restapi.PSUploader;

@Configuration
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
@Slf4j
public class UploadTest
{
    @Autowired
    PSUploader psUploader;

    @Autowired
    PASDUploader pasdUploader;

    public void infinitTest() throws InterruptedException {
        while (true)
        {
            try {
                pasdUploader.upload();
                psUploader.upload();
            } catch (Exception e) {
                System.out.println(e);
            }
            Thread.sleep(3000);

        }

    }

    @Test
    public void testPS() {
        psUploader.upload();
    }

    @Test
    public void testPASD() {
        pasdUploader.upload();
    }

}
