package service.impl;

import analysis.AnalysisResult;
import analysis.AnalysisUtils;
import analysis.PureChannelAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionPureChannelJobResult;
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
import java.util.Arrays;
import java.util.List;

import static enums.CheckUnitJobResult.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * Сервис проверки результата работы робота, проверяющего ПС
 */
@Service
@Slf4j
public class PureChannel_AnalyzerService implements AnalyzerService<ExecutionPureChannelJobResult> {

    private static final String keyWordsSource = "classpath:key_words.json";
    private static final int similarityThreshold = 85;
    private static final List<String> skippingErrorsKeywords = Arrays.asList(
            "ERR_TUNNEL_CONNECTION_FAILED",
            "ERR_NAME_NOT_RESOLVED"
    );

    @Getter
    private List<KeyWord> keyWords = new ArrayList<>();

    private final ResourceLoader resourceLoader;
    private final PODExchange podExchange;
    private final ClassificationService classificationService;
    private final ScreenshotAnalyzerHelper screenshotAnalyzerHelper;

    public PureChannel_AnalyzerService(
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
    public AnalysisResult analyzeResult(ExecutionPureChannelJobResult result) throws AnalysisException {
        log.info("Анализ сервисом обработки результатов чистого канала. URL запроса: {}", result.getCheckUnit().getValue());

        PureChannelAnalysisResult analysisResult = new PureChannelAnalysisResult();
        analysisResult.setCheckUnit(result.getCheckUnit());

        try {
            prepareResult(analysisResult, result);
        } catch (Exception e) {
            throw new AnalysisException("Ошибка во время анализа ПАСД", e);
        }

        CheckUnitJobResult checkUnitJobResult = obtainResult(analysisResult, result);
        if (result.getScreenshot() != null) {
            analysisResult.setScreenshot(result.getScreenshot());
        }
        analysisResult.setCheckResult(checkUnitJobResult);
        checkFinalUrlForForbidden(analysisResult);
        return analysisResult;
    }

    private void checkFinalUrlForForbidden(PureChannelAnalysisResult analysisResult) {
        Boolean needTestFinalUrl = analysisResult.getNeedTestFinalUrl();
        if (needTestFinalUrl != null && needTestFinalUrl) {
            ResponseStatusString check = podExchange.checkUrl(analysisResult.getPageUrlFinal());
            if (check.isStatus()) {
                analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
                analysisResult.setForbiddenFinalUrl(true);
            } else {
                analysisResult.setCheckResult(DOUBTFUL);
            }

            log.info("Результат проверки URL на нахождение в ЕРДИ: " + check + ", URL = " + analysisResult.getPageUrlFinal());
        }
    }

    private NLPCategory getResultNLP(String url, ExecutionPureChannelJobResult result) {
        if (StringUtils.isEmpty(url)) {
            log.info("NLP не запущен, URL пустой!");
            return NLPCategory.STUB;
        }

        log.info("Запуск проверки на CAPTCHA");
        NLPCategory nlpCategory = classificationService.classify(result.getPageContent(), NLPModel.CAPTCHA_DETECTOR);
        if (nlpCategory.equals(NLPCategory.CAPTCHA)) {
            return nlpCategory;
        }

        log.info("Запуск NLP: " + url);
        String page = clearResult(result.getPageContent());

        nlpCategory = classificationService.classify(page, NLPModel.PAGE_CONTENT_CLASSIFICATOR);
        nlpCategory = nlpCategory == null ? NLPCategory.EXCEPTION : nlpCategory;
        log.info("Результат NLP: " + nlpCategory.getDescription());

        return nlpCategory;
    }

    private String clearResult(String result) {
        result = result != null ? result.replaceAll("\n", " ") : "";
        return Jsoup.parse(result).text();
    }

    protected void prepareResult(PureChannelAnalysisResult analysisResult, ExecutionPureChannelJobResult jobResult) throws IOException {
        String chromeErrorCode = jobResult.getChromeErrorCode();
        Boolean responseError = jobResult.getResponseError();
        String pageContent = jobResult.getPageContent();
        if (pageContent == null)
            pageContent = "";

        analysisResult.setHttpStatus(jobResult.getHttpStatus());
        analysisResult.setHttpHeaders(jobResult.getHttpHeaders());

        analysisResult.setResponseError(responseError);
        analysisResult.setResponseErrorCode(chromeErrorCode);

        if (!responseError) {
            analysisResult.setPageSize(pageContent.length());
            analysisResult.setPageUrlFinal(jobResult.getFinalUrlPage());

            analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(pageContent, keyWords));

            analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(analysisResult.getPageUrlFinal(), pageContent));

            analysisResult.setLinkCount(AnalysisUtils.getLinkCounts(pageContent));

            // сравнение конечного и начального URL
            boolean wasRedirect = false;
            if (!isEmpty(analysisResult.getPageUrlFinal())) {
                wasRedirect = !URLUtils.simpleCompareUrls(analysisResult.getPageUrlFinal(), jobResult.getCheckUnit().getValue());
            }
            analysisResult.setRedirectionDetected(wasRedirect);
        }
    }


    private CheckUnitJobResult obtainResult(PureChannelAnalysisResult analysisResult, ExecutionPureChannelJobResult jobResult) {
        if (analysisResult.hasError()) {
            return obtainErrorResult(analysisResult);
        }

        boolean wasRedirect = analysisResult.getRedirectionDetected() != null && analysisResult.getRedirectionDetected();

        NLPCategory resultNLP = getResultNLP(analysisResult.getPageUrlFinal(), jobResult);
        analysisResult.setResultNLP(resultNLP.getDescription());

        if (wasRedirect && resultNLP.equals(NLPCategory.NO_STUB)) {
            analysisResult.setNeedTestFinalUrl(true);
        }

        switch (resultNLP) {
            case STUB:
                return COMPLETED;
            case NO_STUB:
                return FORBIDDEN_CONTENT_DETECTED;
            case ERROR:
            case CAPTCHA:
                return DOUBTFUL;
            case EXCEPTION:
            default:
                return INTERNAL_ERROR;
        }
    }

    private CheckUnitJobResult obtainErrorResult(PureChannelAnalysisResult analysisResult) {
        String errorCode = analysisResult.getResponseErrorCode();
        StringBuffer details = new StringBuffer();

        if (skippingErrorContains(analysisResult)) {
            return COMPLETED;
        }

        CheckUnitJobResult result = AnalysisUtils.obtainErrorResult(errorCode, details);
        result = result == null ? COMPLETED : result;
        return result;
    }

    private boolean skippingErrorContains(PureChannelAnalysisResult analysisResult) {
        return skippingErrorsKeywords.stream().anyMatch(keywords -> analysisResult.getResponseErrorCode().contains(keywords));
    }
}
