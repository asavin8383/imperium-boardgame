package service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import analysis.AnalysisResult;
import analysis.AnalysisUtils;
import analysis.StubAnalysis;
import analysis.VpnAnalysisResult;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import model.KeyWord;
import service.AnalyzerService;

import static enums.CheckUnitJobResult.*;


/**
 * Сервис проверки результата работы робота, проверяющего ПС
 *
 */
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

			// сравнение конечного и начального URL
			boolean wasRedirect = false;
			if (!StringUtils.isEmpty(analysisResult.getPageUrlFinal())){
				wasRedirect = !AnalysisUtils.simpleCompareUrls(analysisResult.getPageUrlFinal(), result.getCheckUnit().getValue());
			}
			analysisResult.setRedirectionDetected(wasRedirect);
		}
	}


	protected CheckUnitJobResult obtainResult(VpnAnalysisResult aRes, ExecutionVpnJobResult jobRes) {
		String errorCode = aRes.getResponseErrorCode();
		boolean wasRedirect = aRes.getRedirectionDetected();

		if (!StringUtils.isEmpty(errorCode)){
			CheckUnitJobResult errorResult = HTTP_SERVER_SEND_NO_RESPONSE;

			if (errorCode.isEmpty())
				errorResult = TIMEOUT_ERROR;

			else if (errorCode.contains("TIMEOUT") || errorCode.contains("TIME_OUT"))
				errorResult = TIMEOUT_ERROR;

			else if (errorCode.contains("DNS"))
				errorResult = DNS_ERROR;

			else if (errorCode.contains("SOCKET")){
				errorResult = SOCKET_ERROR;
			}

			if (errorResult == SOCKET_ERROR){
				aRes.setStubScoreInfo("При доступе к интернет ресурсу возникла следующая ошибка: " + errorCode + ". Вероятно проблемы с сетью.");
				return DOUBTFUL;
			}

			aRes.setStubScoreInfo("При доступе к интернет ресурсу возникла следующая ошибка: " + errorCode);

			return COMPLETED;
		}

		// конечный URL совпадает с vpn - заглушкой
		if (StubAnalysis.checkStubUrl(aRes.getPageUrlFinal(), aRes.getStubUrl())){
			return COMPLETED;
		}

		// сравнение контента иходника с эталоном
        if (useEtalon(jobRes)){
            if (aRes.getSimilarityOriginPercent() >= 90){
                return FORBIDDEN_CONTENT_DETECTED;
            }
        }

		// проверка на заглушку
		boolean isStub = StubAnalysis.isStub(aRes);

		if (isStub){
			return COMPLETED;
		}

		// флаг необходимости проверить конечный юрл в картотеке ЕРДИ
		if (wasRedirect){
			aRes.setNeedTestFinalUrl(true);
			return COMPLETED;
		}

		return FORBIDDEN_CONTENT_DETECTED;
	}

	protected boolean useEtalon(ExecutionVpnJobResult jobRes){
        return !StringUtils.isEmpty(jobRes.getPageContentEtalon()) || !StringUtils.isEmpty(jobRes.getChromeErrorCodeEtalon());
    }

}
