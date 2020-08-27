package service.impl;

import analysis.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import model.NLPCategory;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import rest.ResponseStatusString;
import restapi.PODExchange;
import service.AnalyzerService;
import service.ClassificationService;
import utils.ScreenshotAnalyzerHelper;
import utils.URLUtils;

import javax.annotation.PostConstruct;
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
@Slf4j
public class VPN_AnalyzerService implements AnalyzerService<ExecutionVpnJobResult> {

	private static final String keyWordsSource = "classpath:key_words.json";
	private static final int similarityThreshold = 85;

	@Value("${source_path_vpn}")
	public String sourcePath;

	@Getter
	private List<KeyWord> keyWords = new ArrayList<>();

	private final ResourceLoader resourceLoader;
	private final PODExchange podExchange;
	private final ClassificationService classificationService;
	private final ScreenshotAnalyzerHelper screenshotAnalyzerHelper;

	public VPN_AnalyzerService(
			ResourceLoader resourceLoader,
			PODExchange podExchange,
			@Qualifier("openNLPClassificator") ClassificationService classificationService,
			ScreenshotAnalyzerHelper screenshotAnalyzerHelper) {

		this.resourceLoader = resourceLoader;
		this.podExchange = podExchange;
		this.classificationService = classificationService;
		this.screenshotAnalyzerHelper = screenshotAnalyzerHelper;
	}

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
		analysisResult.setCheckUnit(result.getCheckUnit());

		try {
			prepareResult(analysisResult, result);
		} catch (Exception e) {
			throw new AnalysisException("Ошибка во время анализа ПАСД", e);
		}

		CheckUnitJobResult checkUnitJobResult = obtainResult(analysisResult, result);
		if(checkUnitJobResult.equals(FORBIDDEN_CONTENT_DETECTED) || checkUnitJobResult.equals(DOUBTFUL) || screenshotAnalyzerHelper.screenshotRequired(result.getAccessTool())) {
			analysisResult.setScreenshot(result.getScreenshot());
			analysisResult.setEtalonScreenshot(result.getEtalonScreenshot());
		}
		analysisResult.setCheckResult(checkUnitJobResult);
		checkFinalUrlForForbidden(analysisResult);
		saveSources(analysisResult, result, checkUnitJobResult);
		return analysisResult;
	}

	private void saveSources(VpnAnalysisResult analysisResult, ExecutionVpnJobResult result, CheckUnitJobResult checkUnitJobResult){
		if (
			!analysisResult.hasError() &&
			(checkUnitJobResult == DOUBTFUL || checkUnitJobResult == COMPLETED) &&
			!StringUtils.isEmpty(sourcePath)
		){
			Path path = Paths.get(sourcePath);

			String pageContent = result.getPageContent();
			String pageContentEtalon = result.getPageContentEtalon();

			pageContent = "STUB "  + clearResult(pageContent);
			pageContentEtalon = "NO_STUB " + clearResult(pageContentEtalon);
			String fullContent = result.getFinalUrlPageEtalon() + "\n" + pageContent + "\n" + pageContentEtalon + "\n\n";

			try {
				Files.write(path, fullContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new AnalysisException("Error write sources to file! " + path);
			}
		}
	}

	private void checkFinalUrlForForbidden(VpnAnalysisResult analysisResult){
		Boolean needTestFinalUrl = analysisResult.getNeedTestFinalUrl();
		if (needTestFinalUrl != null && needTestFinalUrl){
			String additionalInfo = "";
			ResponseStatusString check = podExchange.checkUrl(analysisResult.getFinalUrl());
			if (check.isStatus()){
				analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
				analysisResult.setForbiddenFinalUrl(true);
				additionalInfo = "Обнаружен редирект на запрещенный ресурс. ЕРДИ ID:" + check.getResponse() + ".";
			} else {
				analysisResult.setCheckResult(DOUBTFUL);
				additionalInfo = "Обнаружен редирект на ресурс, не содержащийся в ЕРДИ.";
			}
			String info = analysisResult.getStubScoreInfo();
			info = info == null ? "" : info + ". ";
			analysisResult.setStubScoreInfo(info + additionalInfo);

			log.info("Результат проверки URL на находжение в ЕРДИ: " + check + ", URL = " + analysisResult.getPageUrlFinal());
		}
	}

	private NLPCategory getResultNLP(String url, ExecutionVpnJobResult result){
		if (StringUtils.isEmpty(url)){
			log.info("NLP не запущен, URL пустой!");
			return NLPCategory.STUB;
		}

		log.info("Запуск NLP: " + url);
		String page = clearResult(result.getPageContent());

		NLPCategory nlpCategory = classificationService.classify(page);
		nlpCategory = nlpCategory == null ? NLPCategory.EXCEPTION : nlpCategory;
		log.info("Результат NLP: " + nlpCategory.getDescription());

		return nlpCategory;
	}

	private String clearResult(String result){
		result = result != null ? result.replaceAll("\n", " ") : "";
		return Jsoup.parse(result).text();
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
		aRes.setUseStubUrl(jobRes.getUseStubUrl() == null || jobRes.getUseStubUrl());

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
				wasRedirect = !URLUtils.simpleCompareUrls(aRes.getPageUrlFinal(), jobRes.getCheckUnit().getValue());
			}
			aRes.setRedirectionDetected(wasRedirect);
		}
	}


	private CheckUnitJobResult obtainResult(VpnAnalysisResult aRes, ExecutionVpnJobResult jobRes) {
        if (aRes.hasError()) {
            return obtainErrorResult(aRes);
        }

		boolean wasRedirect = aRes.getRedirectionDetected() != null && aRes.getRedirectionDetected();

		// конечный URL совпадает с vpn - заглушкой
		if (aRes.getUseStubUrl()){
			if (URLUtils.compareDomainsInUrls(aRes.getPageUrlFinal(), aRes.getStubUrl())){
				appendInfo(aRes, "Определена заглушка по URL заглушки");
				return COMPLETED;
			}
		}

		if (aRes.getUseEtalon()){
			// проверка на критическую ошибку эталона
			if (aRes.hasEtalonError()){
				CheckUnitJobResult criticalErrorResult = obtainErrorEtalon(aRes);
				if (criticalErrorResult != null){
					return criticalErrorResult;
				}
				else {
					// алгоритм без эталона!
				}
			}
			else {
				// проверяем эталон и оригинал на сходство
				boolean isSimilarityEtalon = (aRes.getSimilarityOriginPercent() >= similarityThreshold);
				if (isSimilarityEtalon){
					if (wasRedirect){
						appendInfo(aRes, String.format("Сходство текста %d%% (порог: %d%%), но произошел редирект.",
								aRes.getSimilarityOriginPercent(), similarityThreshold));
						aRes.setNeedTestFinalUrl(true);
						return DOUBTFUL;
					}
					else {
						appendInfo(aRes, String.format("Сходство текста %d%% (порог: %d%%).",
								aRes.getSimilarityOriginPercent(), similarityThreshold));
						return FORBIDDEN_CONTENT_DETECTED;
					}
				}
				else {
					// алгоритм без эталона!
				}
			}
		}

		// проверка на маленькую заглушку (пустую страницу)
		//!!!!!!Маленькая заглушка путает алгоритм распознавания. Проверка убрана из релиза!!!!!!!
		//StringBuffer stubLittleDetails = new StringBuffer();
		//boolean isEmptyPage = StubAnalysis.isLittleStub(aRes, jobRes.getPageContent(), stubLittleDetails);
		//appendInfo(aRes, stubLittleDetails.toString());

		NLPCategory resultNLP = getResultNLP(aRes.getPageUrlFinal(), jobRes);
		aRes.setResultNLP(resultNLP.getDescription());
		appendInfo(aRes, String.format("Результат NLP: %s.", resultNLP.getDescription()));

		/*if (isEmptyPage){
			return COMPLETED;
		}*/

		if (wasRedirect && resultNLP.equals(NLPCategory.NO_STUB)){
			aRes.setNeedTestFinalUrl(true);
		}

		switch (resultNLP) {
			case STUB:
				return COMPLETED;
			case NO_STUB:
				return FORBIDDEN_CONTENT_DETECTED;
			case ERROR:
				return DOUBTFUL;
			case EXCEPTION:
			default:
				return INTERNAL_ERROR;
		}
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
