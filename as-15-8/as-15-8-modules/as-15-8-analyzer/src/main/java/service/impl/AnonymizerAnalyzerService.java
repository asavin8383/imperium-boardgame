package service.impl;

import analysis.AnalysisResult;
import analysis.AnalysisUtils;
import analysis.AnonymizerAnalysisResult;
import analysis.StubAnalysis;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionAnonymizerResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import service.AnalyzerService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static enums.CheckUnitJobResult.*;

@Slf4j
@Service
public class AnonymizerAnalyzerService implements AnalyzerService<ExecutionAnonymizerResult> {

	private static final String keyWordsSource = "key_words.json";

	//private static final int similarityThreshold = 70;

	@Getter
	private List<KeyWord> keyWords = new ArrayList<>();

	@PostConstruct
	public void initAnalyzer() {
		try {
			File file = new ClassPathResource(keyWordsSource).getFile();
			ObjectMapper mapper = new ObjectMapper();
			keyWords = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, KeyWord.class));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public AnalysisResult analyzeResult(ExecutionAnonymizerResult executionResult) throws AnalysisException {
		AnonymizerAnalysisResult analysisResult = new AnonymizerAnalysisResult();
		analysisResult.setJobID(executionResult.getJobID());
		analysisResult.setCheckUnit(executionResult.getCheckUnit());

		/* ERROR */

		analysisResult.setErrorCode(executionResult.getErrorCode());
		analysisResult.setScreenshot(executionResult.getScreenshot());
		analysisResult.setPageSize(sizeOf(executionResult.getPageContent()));

		if (analysisResult.hasError()) {
			analysisResult.setCheckResult(
					obtainErrorResult(analysisResult.getErrorCode()));
			return analysisResult;
		}

		/* STUB */

		analysisResult.setStubUrl(executionResult.getStubUrl());
		analysisResult.setFinalUrl(executionResult.getFinalUrl());

		try {
			if (isStub(analysisResult, executionResult)) {
				analysisResult.setCheckResult(COMPLETED);
				return analysisResult;
			}
		} catch (IOException e) {
			String msg = "Ошибка при проверке заглушки";
			log.error(msg, e);
			throw new AnalysisException(msg, e);
		}

		/* ETALON */

		analysisResult.setEtalonErrorCode(executionResult.getEtalonErrorCode());
		analysisResult.setEtalonScreenshot(executionResult.getEtalonScreenshot());
		analysisResult.setEtalonPageSize(sizeOf(executionResult.getEtalonPageContent()));

		if ( !analysisResult.hasEtalonError() ) {
			try {
				analysisResult.setSimilarityPercent(
						AnalysisUtils.getTextSimilarityPercent(
								executionResult.getPageContent(),
								executionResult.getEtalonPageContent()));

				// if (analysisResult.getSimilarityPercent() > similarityThreshold)

			} catch (IOException e) {
				String msg = "Ошибка при сверке с эталоном";
				log.error(msg, e);
				throw new AnalysisException(msg, e);
			}
		}

		analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
		return analysisResult;
	}

	private CheckUnitJobResult obtainErrorResult(String errorCode) {

		if (errorCode.contains("TIMEOUT") || errorCode.contains("TIME_OUT"))
			return TIMEOUT_ERROR;

		else if (errorCode.contains("DNS"))
			return DNS_ERROR;

		else if (errorCode.contains("SOCKET"))
			return SOCKET_ERROR;

		return HTTP_SERVER_SEND_NO_RESPONSE;
	}

	private boolean isStub(AnonymizerAnalysisResult analysisResult,
						   ExecutionAnonymizerResult executionResult)
			throws IOException {

		if (checkStubUrl(executionResult.getFinalUrl(), executionResult.getStubUrl()))
			return true;

		String content = executionResult.getPageContent();
		String checkValue = executionResult.getCheckUnit().getValue();

		analysisResult.setLinkCount(AnalysisUtils.getLinkCounts(content));
		analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(content, keyWords));
		analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(checkValue, content));

		return StubAnalysis.isStub(analysisResult);
	}

	private boolean checkStubUrl(String finalUrl, String stubUrl) {
		if (StringUtils.isEmpty(finalUrl) || StringUtils.isEmpty(stubUrl))
			return false;

		finalUrl = finalUrl.toLowerCase();
		stubUrl = stubUrl.toLowerCase();

		return finalUrl.contains(stubUrl) ||
				AnalysisUtils.simpleCompareUrls(finalUrl, stubUrl);
	}

	private Integer sizeOf(String pageContent) {
		return StringUtils.isEmpty(pageContent) ? 0 : pageContent.length();
	}

}

