package service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

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
import static org.apache.commons.lang3.StringUtils.isEmpty;


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

	protected void prepareResult(VpnAnalysisResult aRes, ExecutionVpnJobResult jobRes) throws IOException {
		String chromeErrorCode = jobRes.getChromeErrorCode();
		String chromeErrorCodeEtalon = jobRes.getChromeErrorCodeEtalon();
		Boolean responseError = jobRes.getResponseError();
		String  pageContent = jobRes.getPageContent();
		if (pageContent == null)
			pageContent = "";
		String pageContentEtalon = jobRes.getPageContentEtalon();
		if (pageContentEtalon == null)
			pageContentEtalon = "";

		aRes.setResponseError(responseError);
		aRes.setResponseErrorCode(chromeErrorCode);
		aRes.setResponseErrorCodeEtalon(chromeErrorCodeEtalon);
		aRes.setUseEtalon(jobRes.getUseEtalon() == null || jobRes.getUseEtalon());

		if (!responseError) {
			aRes.setPageSize(pageContent.length());
			aRes.setPageSizeEtalon(pageContentEtalon.length());
			aRes.setPageUrlFinal(jobRes.getFinalUrlPage());
			aRes.setPageUrlFinalEtalon(jobRes.getFinalUrlPageEtalon());
			aRes.setStubUrl(jobRes.getStubUrl());

			aRes.setKeyWordsCount(AnalysisUtils.getCountKeyWords(pageContent, keyWords));

			aRes.setDomainNameCount(AnalysisUtils.getDomainCount(jobRes.getCheckUnit().getValue(), pageContent));

			aRes.setSimilarityOriginPercent(AnalysisUtils.getTextSimilarityPercent(pageContent, pageContentEtalon));

			aRes.setLinkCount(AnalysisUtils.getLinkCounts(pageContent));

			// сравнение конечного и начального URL
			boolean wasRedirect = false;
			if (!isEmpty(aRes.getPageUrlFinal())){
				wasRedirect = !AnalysisUtils.simpleCompareUrls(aRes.getPageUrlFinal(), jobRes.getCheckUnit().getValue());
			}
			aRes.setRedirectionDetected(wasRedirect);
		}
	}


	protected CheckUnitJobResult obtainResult(VpnAnalysisResult aRes, ExecutionVpnJobResult jobRes) {
		String errorCode = aRes.getResponseErrorCode();
		boolean wasRedirect = aRes.getRedirectionDetected();

		if (!isEmpty(errorCode)){
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
        if (aRes.getUseEtalon()){
        	if (!aRes.hasEtalonError()){
				if (aRes.getSimilarityOriginPercent() >= 90){
					return FORBIDDEN_CONTENT_DETECTED;
				}
			}
            else {
				appendInfo(aRes, "Не удалось загрузить эталон: " + aRes.getResponseErrorCodeEtalon());
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

	private void appendInfo(VpnAnalysisResult vpnAnalysisResult, String append){
		String info = vpnAnalysisResult.getStubScoreInfo();
		info = info == null ? "" : info;
		info += info.isEmpty() ? "" : ". ";
		info += append;
		vpnAnalysisResult.setStubScoreInfo(info);
	}

}
