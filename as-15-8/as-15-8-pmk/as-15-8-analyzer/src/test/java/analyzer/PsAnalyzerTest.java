package analyzer;

import analysis.AnalysisResult;
import analysis.PsAnalysisJobResult;
import checkUnits.CheckUnit;
import common.AnalyzerProperties;
import common.ApplicationConfiguration;
import enums.CheckUnitJobResult;
import execution.ExecutionPSJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import service.impl.PS_AnalyzerService;

import static org.junit.Assert.assertNotNull;

/**
 * Created by san
 * Date: 26.02.2020
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class PsAnalyzerTest {

    @Autowired
    PS_AnalyzerService psAnalyzerService;

    @Test
    public void testDetailResult(){
        ExecutionPSJobResult executionPSJobResult = new ExecutionPSJobResult();
        executionPSJobResult.setAccessTool("yandex");
        executionPSJobResult.setCheckUnitJobResult(CheckUnitJobResult.COMPLETED);
        executionPSJobResult.setCheckUnit(new CheckUnit());
        PsAnalysisJobResult analysisResult = (PsAnalysisJobResult)psAnalyzerService.analyzeResult(executionPSJobResult);
        assertNotNull(analysisResult);
        assertNotNull(analysisResult.getDescription());
        System.out.println(analysisResult.getDescription());
    }
}
