package service.impl;

import analysis.AnalysisResult;
import analysis.PS_AnalysisJobResult;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import restapi.PODExchange;
import service.AnalyzerService;

import java.util.ArrayList;
import java.util.List;

import static enums.CheckUnitJobResult.COMPLETED;
import static enums.CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED;

/**
 * Сервис проверки результата работы робота, проверяющего ПС
 * @author shabalinAI
 *
 */
@Service
@Slf4j
public class PS_AnalyzerService implements AnalyzerService<ExecutionPSJobResult> {

	@Autowired
	private PODExchange podExchange;

	@Override
	public AnalysisResult analyzeResult(ExecutionPSJobResult result) {
		PS_AnalysisJobResult analysisResult = new PS_AnalysisJobResult();
		analysisResult.setJobID(result.getJobID());
		analysisResult.setCheckUnit(result.getCheckUnit());
		analysisResult.setCheckResult(obtainResult(result, analysisResult));
		analysisResult.setScreenshot(result.getScreenshot());
		analysisResult.setEtalonScreenshot(result.getEtalonScreenshot());
		return analysisResult;
	}

	private CheckUnitJobResult obtainResult(ExecutionPSJobResult result, PS_AnalysisJobResult analysisResult) {
		CheckUnit checkUnit = result.getCheckUnit();
		if (checkUnit.getType() == CheckUnitType.SEARCH_PHRASE){
			List<String> urls = result.getUrls() == null ? new ArrayList<>() : result.getUrls();

			String description = "Запрос проверки по фразам: " + result.getCheckUnit().getValue() + "\n";
			description += "Спиосок найденных URL:\n";
			for(String url : urls){
				description += url + "\n";
			}
			description += "\n";
			description += "Список запрещенных URL:\n";

			List<String> findUrls = new ArrayList<>();
			for(String url : urls){
				boolean check = podExchange.checkUrl(url);
				if (check){
					findUrls.add(url);

				}
			}
			for(String url : findUrls){
				description += url + "\n";
			}
			if (findUrls.size() == 0){
				description += "<записи отсутствуют>";
			}
			analysisResult.setDescription(description);

			log.info("Результат проверки URL-ов (по фразе '{}'): кол-во найденных в ЕРДИ: {}",
					checkUnit.getValue(), findUrls.size());

			return findUrls.size() > 0 ? FORBIDDEN_CONTENT_DETECTED : COMPLETED;
		}
		else {
			return result.isLinkFound() ? FORBIDDEN_CONTENT_DETECTED : COMPLETED;
		}
	}
}
