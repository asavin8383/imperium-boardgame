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
import org.springframework.util.StringUtils;
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
		boolean wasRedirect = aRes.getRedirectionDetected();

        if (aRes.hasError()) {
            aRes.setCheckResult(obtainErrorResult(aRes));
            return aRes.getCheckResult();
        }

		if (aRes.hasEtalonError()){
            obtainErrorEtalon(aRes);
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

	private CheckUnitJobResult obtainErrorResult(VpnAnalysisResult aRes){
        String errorCode = aRes.getResponseErrorCode();
        StringBuffer details = new StringBuffer();

		CheckUnitJobResult result = AnalysisUtils.obtainErrorResult(errorCode, details);
		appendInfo(aRes, details.toString());

        return result == null ? COMPLETED : result;
    }

    private void obtainErrorEtalon(VpnAnalysisResult aRes){
        String errorCodeEtalon = aRes.getResponseErrorCodeEtalon();

        if (!isEmpty(errorCodeEtalon)){
            if (errorCodeEtalon.contains("NO_INTERNET")) {
                aRes.setStubScoreInfo("Ошибка получения эталона! Нет интернета! " + errorCodeEtalon);
            }
            else {
                aRes.setStubScoreInfo("Ошибка получения эталона! " + errorCodeEtalon);
            }
        }
    }

	private void appendInfo(VpnAnalysisResult aRes, String append){
		String info = aRes.getStubScoreInfo();
		info = info == null ? "" : info;
		append = append == null ? "" : append;
		info += (StringUtils.isEmpty(info) || StringUtils.isEmpty(append) ? "" : ". ") + append;
		aRes.setStubScoreInfo(info);
	}

}
