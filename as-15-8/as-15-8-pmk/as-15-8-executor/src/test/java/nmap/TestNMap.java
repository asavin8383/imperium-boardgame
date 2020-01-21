package nmap;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import com.fasterxml.jackson.databind.ObjectMapper;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import service.CheckUnitVerificationServiceFactory;
import service.impl.NmapServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CheckUnitVerificationServiceFactory.class, NmapServiceImpl.class})
@PropertySource("file:config/application.yml")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestNMap {

    private final CheckUnitVerificationServiceFactory checkUnitVerificationServiceFactory;

    @Test
    public void testNMap() {
        CheckUnitJob checkUnitJob = new CheckUnitJob();
        checkUnitJob.setAccessTool("vpn");
        checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.IP_V4, "174.138.5.46"));

        try {
            ExecutionJobResult executionJobResult = checkUnitVerificationServiceFactory
                    .getService(checkUnitJob)
                    .run(1L, checkUnitJob);

            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(executionJobResult));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
