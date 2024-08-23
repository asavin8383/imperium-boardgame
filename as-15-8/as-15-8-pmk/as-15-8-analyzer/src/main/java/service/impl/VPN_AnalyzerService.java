package service.impl;

import analysis.AnalysisResult;
import analysis.AnalysisUtils;
import analysis.VpnAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import model.NLPCategory;
import model.NLPModel;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.ArrayList;
import java.util.List;

import static enums.CheckUnitJobResult.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * Сервис проверки результата работы робота, проверяющего ПС
 */
@Service
@Slf4j
public class VPN_AnalyzerService implements AnalyzerService<ExecutionVpnJobResult> {

    private static final String keyWordsSource = "classpath:key_words.json";
    private static final int similarityThreshold = 85;

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
        } catch (IOException e) {
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
        if (checkUnitJobResult.equals(FORBIDDEN_CONTENT_DETECTED) || checkUnitJobResult.equals(DOUBTFUL) || screenshotAnalyzerHelper.screenshotRequired(result.getAccessTool())) {
            analysisResult.setScreenshot(result.getScreenshot());
            analysisResult.setEtalonScreenshot(result.getEtalonScreenshot());
        }
        analysisResult.setCheckResult(checkUnitJobResult);
        checkFinalUrlForForbidden(analysisResult);
        return analysisResult;
    }

    private void checkFinalUrlForForbidden(VpnAnalysisResult analysisResult) {
        Boolean needTestFinalUrl = analysisResult.getNeedTestFinalUrl();
        if (needTestFinalUrl != null && needTestFinalUrl) {
            String additionalInfo;
            ResponseStatusString check = podExchange.checkUrl(analysisResult.getFinalUrl());
            if (check.isStatus()) {
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

    private NLPCategory getResultNLP(String url, ExecutionVpnJobResult result) {
        if (StringUtils.isEmpty(url)) {
            log.info("NLP не запущен, URL пустой!");
            return NLPCategory.STUB;
        }

        log.info("Запуск NLP: " + url);
        String page = clearResult(result.getPageContent());

        NLPCategory nlpCategory = classificationService.classify(page, NLPModel.PAGE_CONTENT_CLASSIFICATOR);
        nlpCategory = nlpCategory == null ? NLPCategory.EXCEPTION : nlpCategory;
        log.info("Результат NLP: " + nlpCategory.getDescription());

        return nlpCategory;
    }

    private String clearResult(String result) {
        result = result != null ? result.replaceAll("\n", " ") : "";
        return Jsoup.parse(result).text();
    }

    protected void prepareResult(VpnAnalysisResult analysisResult, ExecutionVpnJobResult jobResult) throws IOException {
        String chromeErrorCode = jobResult.getChromeErrorCode();
        String chromeErrorCodeEtalon = jobResult.getChromeErrorCodeEtalon();
        Boolean responseError = jobResult.getResponseError();
        String pageContent = jobResult.getPageContent();
        if (pageContent == null)
            pageContent = "";
        String pageContentEtalon = jobResult.getPageContentEtalon();
        if (pageContentEtalon == null)
            pageContentEtalon = "";

        analysisResult.setHttpStatus(jobResult.getHttpStatus());
        analysisResult.setHttpStatusEtalon(jobResult.getHttpStatusEtalon());
        analysisResult.setHttpHeaders(jobResult.getHttpHeaders());
        analysisResult.setHttpHeadersEtalon(jobResult.getHttpHeadersEtalon());

        analysisResult.setResponseError(responseError);
        analysisResult.setResponseErrorCode(chromeErrorCode);
        analysisResult.setResponseErrorCodeEtalon(chromeErrorCodeEtalon);
        analysisResult.setUseEtalon(jobResult.getUseEtalon() == null || jobResult.getUseEtalon());
        analysisResult.setUseStubUrl(jobResult.getUseStubUrl() == null || jobResult.getUseStubUrl());

        if (!responseError) {
            analysisResult.setPageSize(pageContent.length());
            analysisResult.setPageSizeEtalon(pageContentEtalon.length());
            analysisResult.setPageUrlFinal(jobResult.getFinalUrlPage());
            analysisResult.setPageUrlFinalEtalon(jobResult.getFinalUrlPageEtalon());
            analysisResult.setStubUrl(jobResult.getStubUrl());

            analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(pageContent, keyWords));

            analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(analysisResult.getPageUrlFinal(), pageContent));

            analysisResult.setSimilarityOriginPercent(AnalysisUtils.getTextSimilarityPercent(pageContent, pageContentEtalon));

            analysisResult.setLinkCount(AnalysisUtils.getLinkCounts(pageContent));

            // сравнение конечного и начального URL
            boolean wasRedirect = false;
            if (!isEmpty(analysisResult.getPageUrlFinal())) {
                wasRedirect = !URLUtils.simpleCompareUrls(analysisResult.getPageUrlFinal(), jobResult.getCheckUnit().getValue());
            }
            analysisResult.setRedirectionDetected(wasRedirect);
        }
    }


    private CheckUnitJobResult obtainResult(VpnAnalysisResult analysisResult, ExecutionVpnJobResult jobResult) {
        if (analysisResult.hasError()) {
            return obtainErrorResult(analysisResult);
        }

        boolean wasRedirect = analysisResult.getRedirectionDetected() != null && analysisResult.getRedirectionDetected();

        // конечный URL совпадает с vpn - заглушкой
        if (analysisResult.getUseStubUrl()) {
            if (URLUtils.compareDomainsInUrls(analysisResult.getPageUrlFinal(), analysisResult.getStubUrl())) {
                appendInfo(analysisResult, "Определена заглушка по URL заглушки");
                return COMPLETED;
            }
        }

        if (analysisResult.getUseEtalon()) {
            // проверка на критическую ошибку эталона
            if (analysisResult.hasEtalonError()) {
                CheckUnitJobResult criticalErrorResult = obtainErrorEtalon(analysisResult);
                if (criticalErrorResult != null) {
                    return criticalErrorResult;
                } else {
                    // алгоритм без эталона!
                }
            } else {
                // проверяем эталон и оригинал на сходство
                boolean isSimilarityEtalon = (analysisResult.getSimilarityOriginPercent() >= similarityThreshold);
                if (isSimilarityEtalon) {
                    if (wasRedirect) {
                        appendInfo(analysisResult, String.format("Сходство текста %d%% (порог: %d%%), но произошел редирект.",
                                analysisResult.getSimilarityOriginPercent(), similarityThreshold));
                        analysisResult.setNeedTestFinalUrl(true);
                        return DOUBTFUL;
                    } else {
                        appendInfo(analysisResult, String.format("Сходство текста %d%% (порог: %d%%).",
                                analysisResult.getSimilarityOriginPercent(), similarityThreshold));
                        return FORBIDDEN_CONTENT_DETECTED;
                    }
                } else {
                    // алгоритм без эталона!
                }
            }
        }

        // проверка на маленькую заглушку (пустую страницу)
        //!!!!!!Маленькая заглушка путает алгоритм распознавания. Проверка убрана из релиза!!!!!!!
        //StringBuffer stubLittleDetails = new StringBuffer();
        //boolean isEmptyPage = StubAnalysis.isLittleStub(aRes, jobRes.getPageContent(), stubLittleDetails);
        //appendInfo(aRes, stubLittleDetails.toString());

        NLPCategory resultNLP = getResultNLP(analysisResult.getPageUrlFinal(), jobResult);
        analysisResult.setResultNLP(resultNLP.getDescription());
        appendInfo(analysisResult, String.format("Результат NLP: %s.", resultNLP.getDescription()));

		/*if (isEmptyPage){
			return COMPLETED;
		}*/

        if (wasRedirect && resultNLP.equals(NLPCategory.NO_STUB)) {
            analysisResult.setNeedTestFinalUrl(true);
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

    private CheckUnitJobResult obtainErrorResult(VpnAnalysisResult analysisResult) {
        String errorCode = analysisResult.getResponseErrorCode();
        StringBuffer details = new StringBuffer();

        CheckUnitJobResult result = AnalysisUtils.obtainErrorResult(errorCode, details);
        result = result == null ? COMPLETED : result;

        appendInfo(analysisResult, details.toString());
        return result;
    }

    private CheckUnitJobResult obtainErrorEtalon(VpnAnalysisResult analysisResult) {
        String errorCodeEtalon = analysisResult.getResponseErrorCodeEtalon();
        StringBuffer details = new StringBuffer();

        CheckUnitJobResult result = AnalysisUtils.obtainErrorResultEtalon(errorCodeEtalon, details);
        appendInfo(analysisResult, details.toString());

        return result;
    }

    private void appendInfo(VpnAnalysisResult analysisResult, String append) {
        analysisResult.setStubScoreInfo(AnalysisUtils.appendString(analysisResult.getStubScoreInfo(), append));
    }
}
