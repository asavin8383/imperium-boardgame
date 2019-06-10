package service.impl;

import analysis.AnalysisResult;
import analysis.AnalysisUtils;
import analysis.StubAnalysis;
import analysis.VpnAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import service.AnalyzerService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static enums.CheckUnitJobResult.*;


/**
 * Сервис проверки результата работы робота, проверяющего ПС
 *
 */
@Slf4j
@Service
public class VPN_AnalyzerService implements AnalyzerService<ExecutionVpnJobResult> {

	public static String keyWordsSource = "key_words.json";

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
	public AnalysisResult analyzeResult(ExecutionVpnJobResult result) throws AnalysisException {
		VpnAnalysisResult analysisResult = new VpnAnalysisResult();
		analysisResult.setJobID(result.getJobID());
		analysisResult.setCheckUnit(result.getCheckUnit());
		analysisResult.setScreenshot(result.getScreenshot());
		analysisResult.setEtalonScreenshot(result.getEtalonScreenshot());

		try {
			prepareResult(analysisResult, result);
		} catch (Exception e) {
			throw new AnalysisException(String.format("Ошибка во время анализа ПАСД (jobID=%d)", result.getJobID()), e);
		}

		analysisResult.setCheckResult(obtainResult(analysisResult, result));
		return analysisResult;
	}

	protected void prepareResult(VpnAnalysisResult analysisResult, ExecutionVpnJobResult result) throws IOException {
		String chromeErrorCode = result.getChromeErrorCode();
		String chromeErrorCodeEtalon = result.getChromeErrorCodeEtalon();
		Boolean responseError = result.getResponseError();
		String  pageContent = result.getPageContent();
		if (pageContent == null)
			pageContent = "";
		String pageContentEtalon = result.getPageContentEtalon();
		if (pageContentEtalon == null)
			pageContentEtalon = "";

		analysisResult.setResponseError(responseError);
		analysisResult.setResponseErrorCode(chromeErrorCode);
		analysisResult.setResponseErrorCodeEtalon(chromeErrorCodeEtalon);

		if (!responseError) {
			analysisResult.setPageSize(pageContent.length());
			analysisResult.setPageSizeEtalon(pageContentEtalon.length());
			analysisResult.setPageUrlFinal(result.getFinalUrlPage());
			analysisResult.setPageUrlFinalEtalon(result.getFinalUrlPageEtalon());
			analysisResult.setStubUrl(result.getStubUrl());

			analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(pageContent, keyWords));

			analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(result.getCheckUnit().getValue(), pageContent));

			analysisResult.setSimilarityOriginPercent(AnalysisUtils.getTextSimilarityPercent(pageContent, pageContentEtalon));

			analysisResult.setLinkCount(AnalysisUtils.getLinkCounts(pageContent));
		}
	}


	protected CheckUnitJobResult obtainResult(VpnAnalysisResult aRes, ExecutionVpnJobResult jobRes) {

		if (aRes.getResponseError()){
			String errorCode = aRes.getResponseErrorCode();

			CheckUnitJobResult errorResult = HTTP_SERVER_SEND_NO_RESPONSE;

			if (errorCode == null || errorCode.isEmpty())
				errorResult = TIMEOUT_ERROR;

			else if (errorCode.contains("TIMEOUT") || errorCode.contains("TIME_OUT"))
				errorResult = TIMEOUT_ERROR;

			else if (errorCode.contains("DNS"))
				errorResult = DNS_ERROR;

			else if (errorCode.contains("SOCKET"))
				errorResult = SOCKET_ERROR;

			return errorResult;
		}

		// конечный URL совпадает с vpn - заглушкой
		if (StubAnalysis.checkStubUrl(aRes.getPageUrlFinal(), aRes.getStubUrl())){
			return COMPLETED;
		}

		// сравнение конечного и начального URL
		boolean wasRedirect = !AnalysisUtils.simpleCompareUrls(aRes.getPageUrlFinal(), jobRes.getCheckUnit().getValue());

		// сравнение контента иходника с эталоном
		if (aRes.getSimilarityOriginPercent() >= 90){
			return FORBIDDEN_CONTENT_DETECTED;
		}

		// проверка на заглушку
		boolean isStub = StubAnalysis.isStub(aRes);

		if (isStub){
			return COMPLETED;
		}

		// флаг необходимости проверить конечный юрл в картотеке ЕРДИ
		if (wasRedirect){
			aRes.setNeedTestFinalUrl(true);
			return COMPLETED;	// todo - в случае если юрл разрешен (подумать)
		}

		return FORBIDDEN_CONTENT_DETECTED;	// todo - юрл тот же, но не заглушка (подумать)
	}

}
