package service.impl;

import analysis.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import model.KeyWord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import service.AnalyzerService;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static enums.CheckUnitJobResult.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * Сервис проверки результата работы робота, проверяющего ПС
 *
 */
@Service
public class VPN_AnalyzerService implements AnalyzerService<ExecutionVpnJobResult> {

	public static final String keyWordsSource = "classpath:key_words.json";
	public static final int similarityThreshold = 85;

	@Value("${spring.source_path}")
	public String sourcePath;

	@Getter
	private List<KeyWord> keyWords = new ArrayList<>();

	@Autowired
	private ResourceLoader resourceLoader;
	
	@PostConstruct
	public void initAnalyzer() {
		try {
			InputStream input = resourceLoader.getResource(keyWordsSource).getInputStream();
			ObjectMapper mapper = new ObjectMapper();
			keyWords = mapper.readValue(input, mapper.getTypeFactory().constructCollectionType(List.class, KeyWord.class));
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

		CheckUnitJobResult checkUnitJobResult = obtainResult(analysisResult, result);
		analysisResult.setCheckResult(checkUnitJobResult);
		saveSources(analysisResult, result, checkUnitJobResult);
		return analysisResult;
	}

	protected void saveSources(VpnAnalysisResult analysisResult, ExecutionVpnJobResult result, CheckUnitJobResult checkUnitJobResult){
		if (
			!analysisResult.hasError() &&
			(checkUnitJobResult == DOUBTFUL || checkUnitJobResult == COMPLETED) &&
			!StringUtils.isEmpty(sourcePath)
		){

			String pageContent = result.getPageContent();
			String pageContentEtalon = result.getPageContentEtalon();

			pageContent = "STUB "  + (pageContent != null ? pageContent.replaceAll("\n", " ") : "");
			pageContentEtalon = "NO_STUB " + (pageContentEtalon != null ? pageContentEtalon.replaceAll("\n", " ") : "");
			String fullContent = pageContent + "\n" + pageContentEtalon + "\n";

			try (FileOutputStream fos = new FileOutputStream(sourcePath)) {
				fos.write(fullContent.getBytes());
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new AnalysisException("Error write sources to file! " + sourcePath);
			}
		}
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

		aRes.setHttpStatus(jobRes.getHttpStatus());
		aRes.setHttpStatusEtalon(jobRes.getHttpStatusEtalon());
		aRes.setHttpHeaders(jobRes.getHttpHeaders());
		aRes.setHttpHeadersEtalon(jobRes.getHttpHeadersEtalon());

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

			aRes.setDomainNameCount(AnalysisUtils.getDomainCount(aRes.getPageUrlFinal(), pageContent));

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
		boolean wasRedirect = aRes.getRedirectionDetected() != null && aRes.getRedirectionDetected();

        if (aRes.hasError()) {
            return obtainErrorResult(aRes);
        }

		// конечный URL совпадает с vpn - заглушкой
		if (AnalysisUtils.compareDomainsInUrls(aRes.getPageUrlFinal(), aRes.getStubUrl())){
			appendInfo(aRes, "Определена заглушка по URL заглушки");
			return COMPLETED;
		}

		boolean useEtalon = aRes.getUseEtalon() == null || aRes.getUseEtalon();

		// проверка на критическую ошибку эталона
		if (useEtalon && aRes.hasEtalonError()){
			CheckUnitJobResult errorResult = obtainErrorEtalon(aRes);
			if (errorResult != null){
				return errorResult;
			}
		}

		// если эталон есть, проверяем на схожесть
		boolean isSimilarityEtalon = false;
		if (useEtalon && !aRes.hasEtalonError()){
			if (aRes.getSimilarityOriginPercent() >= similarityThreshold){
				isSimilarityEtalon = true;
			}
		}

		// проверка на маленькую заглушку
		StringBuffer stubLittleDetails = new StringBuffer();
		boolean isLittleStub = StubAnalysis.isLittleStub(aRes, jobRes.getPageContent(), stubLittleDetails);
		if (isLittleStub){
			appendInfo(aRes, stubLittleDetails.toString());
			return DOUBTFUL;	// todo - так сказали делать
		}

		// проверка на заглушку
		StringBuffer stubDetails = new StringBuffer();
		boolean isStub = StubAnalysis.isStub(aRes, StubAnalysis.getDefaultStubWeights(), stubDetails);
		appendInfo(aRes, stubDetails.toString());
		if (isStub){
			return DOUBTFUL;	// todo - так сказали делать
		}

		// результат по сходству
		if (isSimilarityEtalon){
			if (wasRedirect){
                appendInfo(aRes, "Порог сходства текста >= " + similarityThreshold + "%, но прошел редирект.");
                aRes.setNeedTestFinalUrl(true);
				return DOUBTFUL;
			}
			else {
                appendInfo(aRes, "Порог сходства текста >= " + similarityThreshold + "%.");
				return FORBIDDEN_CONTENT_DETECTED;
			}
		}

		// проверка без эталона
		if (!wasRedirect){
			StringBuffer contentDetails = new StringBuffer();
			boolean isForbidden = ContentAnalysis.forbiddenContent(aRes, contentDetails);
			appendInfo(aRes, contentDetails.toString());
			if (isForbidden){
				return FORBIDDEN_CONTENT_DETECTED;
			}
		}

		if (wasRedirect){
			aRes.setNeedTestFinalUrl(true);
		}

		// не прошли проверку - сомнительно
		return DOUBTFUL;
	}

	private CheckUnitJobResult obtainErrorResult(VpnAnalysisResult aRes){
		String errorCode = aRes.getResponseErrorCode();
		StringBuffer details = new StringBuffer();

		CheckUnitJobResult result = AnalysisUtils.obtainErrorResult(errorCode, details);
		result = result == null ? COMPLETED : result;

		appendInfo(aRes, details.toString());
		return result;
    }

    private CheckUnitJobResult obtainErrorEtalon(VpnAnalysisResult aRes){
        String errorCodeEtalon = aRes.getResponseErrorCodeEtalon();
		StringBuffer details = new StringBuffer();

		CheckUnitJobResult result = AnalysisUtils.obtainErrorResultEtalon(errorCodeEtalon, details);
		appendInfo(aRes, details.toString());

		return result;
    }

	private void appendInfo(VpnAnalysisResult aRes, String append){
		aRes.setStubScoreInfo(AnalysisUtils.appendString(aRes.getStubScoreInfo(), append));
	}

}
