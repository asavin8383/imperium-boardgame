package service.impl;

import analysis.AnalysisResult;
import analysis.NMapAnalysisJobResult;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import execution.NmapExecutionResult;
import org.springframework.stereotype.Service;
import service.AnalyzerService;

import static enums.CheckUnitJobResult.COMPLETED;
import static enums.CheckUnitJobResult.FORBIDDEN_CONTENT_DETECTED;

/**
 * Сервис проверки результата работы Nmap
 * @author shabalinAI
 *
 */
@Service
public class NMap_AnalyzerService implements AnalyzerService<NmapExecutionResult> {

	@Override
	public AnalysisResult analyzeResult(NmapExecutionResult result) {
		NMapAnalysisJobResult analysisResult = new NMapAnalysisJobResult();
		analysisResult.setJobID(result.getJobID());
		analysisResult.setCheckUnit(result.getCheckUnit());
		analysisResult.setCheckResult(obtainResult(result));
		analysisResult.setNmapLog(result.getNmapLog());
		return analysisResult;
	}

	private CheckUnitJobResult obtainResult(NmapExecutionResult result) {
		int openedPortsCount = result.getOpenedPorts().values()
				.stream()
				.mapToInt(portsOfHost -> portsOfHost.size())
				.sum();
		return openedPortsCount > 0 ? FORBIDDEN_CONTENT_DETECTED : COMPLETED;
	}
}
