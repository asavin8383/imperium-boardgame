package nmap;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import service.CheckUnitVerificationServiceFactory;
import service.impl.NmapServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CheckUnitVerificationServiceFactory.class, NmapServiceImpl.class})
@PropertySource("file:config/application.yml")
public class TestNMap {

    @Test
    public void testNMap() {
        CheckUnitJob checkUnitJob = new CheckUnitJob();
        checkUnitJob.setAccessTool("vpn");
        checkUnitJob.setJobID(1L);
        checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.IP_V4, "174.138.5.46"));

        try {
            ExecutionJobResult executionJobResult = CheckUnitVerificationServiceFactory
                    .getService(checkUnitJob.getCheckUnit().getType())
                    .run(checkUnitJob);

            ObjectMapper mapper = new ObjectMapper();
            System.out.println(mapper.writeValueAsString(executionJobResult));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
