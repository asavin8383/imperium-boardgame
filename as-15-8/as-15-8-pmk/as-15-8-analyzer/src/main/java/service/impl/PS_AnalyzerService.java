package service.impl;

import analysis.AnalysisResult;
import analysis.PsAnalysisJobResult;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import enums.CheckUnitJobResult;
import execution.ExecutionPSJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.ResponseStatusString;
import restapi.PODExchange;
import service.AnalyzerService;
import utils.ScreenshotAnalyzerHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static enums.CheckUnitJobResult.COMPLETED;
import static enums.CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED;

/**
 * Сервис проверки результата работы робота, проверяющего ПС
 * @author shabalinAI
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PS_AnalyzerService implements AnalyzerService<ExecutionPSJobResult> {

	private final PODExchange podExchange;
	private final ScreenshotAnalyzerHelper screenshotAnalyzerHelper;

	@Override
	public AnalysisResult analyzeResult(ExecutionPSJobResult result) {
		PsAnalysisJobResult analysisResult = new PsAnalysisJobResult();
		analysisResult.setCheckUnit(result.getCheckUnit());
		CheckUnitJobResult checkUnitJobResult = obtainResult(result, analysisResult);
		analysisResult.setCheckResult(checkUnitJobResult);
		if(checkUnitJobResult.equals(FORBIDDEN_CONTENT_DETECTED) || screenshotAnalyzerHelper.screenshotRequired(result.getAccessTool())){
			analysisResult.setScreenshot(result.getScreenshot());
			analysisResult.setEtalonScreenshot(result.getEtalonScreenshot());
		} else {
			analysisResult.setDescription("В конфигурации робота " + result.getAccessTool() + " установлено не сохранять скриншоты для отсутствия нарушений!");
		}
		return analysisResult;
	}

	private CheckUnitJobResult obtainResult(ExecutionPSJobResult result, PsAnalysisJobResult analysisResult) {
		CheckUnit checkUnit = result.getCheckUnit();
		if (result.getCheckUnitJobResult() != null){
			return result.getCheckUnitJobResult();
		}
		if (checkUnit.getType() == CheckUnitType.SEARCH_PHRASE){
			List<String> urls = result.getUrls() == null ? new ArrayList<>() : result.getUrls();

			StringBuilder description = new StringBuilder("Запрос проверки по фразам: " + result.getCheckUnit().getValue() + "\n");
			description.append("Спиосок найденных URL:\n");
			for(String url : urls){
				description.append(url).append("\n");
			}
			if (urls.size() == 0){
				description.append("<записи отсутствуют>");
			}
			description.append("\n");
			description.append("Список запрещенных URL:\n");

			Map<String, String> findUrls = new LinkedHashMap<>();
			for(String url : urls){
                ResponseStatusString check = podExchange.checkUrl(url);
				if (check.isStatus()){
					findUrls.put(url, check.getResponse());
				}
			}
			for(String url : findUrls.keySet()){
			    String erdiId = findUrls.get(url);
				description.append("ERDI: ").append(erdiId).append(", URL: ").append(url).append("\n");
			}
			if (findUrls.size() == 0){
				description.append("<записи отсутствуют>");
			}
			analysisResult.setDescription(description.toString());

			log.info("Результат проверки URL-ов (по фразе '{}'): кол-во найденных в ЕРДИ: {}",
					checkUnit.getValue(), findUrls.size());

			return findUrls.size() > 0 ? FORBIDDEN_CONTENT_DETECTED : COMPLETED;
		}
		else {
			return result.isLinkFound() ? FORBIDDEN_CONTENT_DETECTED : COMPLETED;
		}
	}
}
