package service.impl;

import analysis.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionAnonymizerResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import model.NLPCategory;
import model.NLPModel;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import service.AnalyzerService;
import service.ClassificationService;
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


@Slf4j
@Service
public class AnonymizerAnalyzerService implements AnalyzerService<ExecutionAnonymizerResult> {

	private static final String keyWordsSource = "classpath:key_words.json";

	private static final int similarityThreshold = 30;

	private static final int EMPTY_PAGE_SIZE = 1024;


	@Value("${source_path_anonym}")
	public String sourcePath;

	@Getter
	private List<KeyWord> keyWords = new ArrayList<>();

	private final ResourceLoader resourceLoader;

	private final ClassificationService classificationService;

	public AnonymizerAnalyzerService(
			ResourceLoader resourceLoader,
			@Qualifier("openNLPClassificator") ClassificationService classificationService) {

		this.resourceLoader = resourceLoader;
		this.classificationService = classificationService;
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
	public AnalysisResult analyzeResult(ExecutionAnonymizerResult executionResult) throws AnalysisException {
		AnonymizerAnalysisResult analysisResult = new AnonymizerAnalysisResult();
		obtainResult(analysisResult, executionResult);
		saveSources(analysisResult, executionResult);

		return analysisResult;
	}

	private void saveSources(AnonymizerAnalysisResult analysisResult, ExecutionAnonymizerResult result){

		CheckUnitJobResult checkUnitJobResult = analysisResult.getCheckResult();

		if (
				!analysisResult.hasError() &&
				(checkUnitJobResult == DOUBTFUL || checkUnitJobResult == COMPLETED) &&
				!StringUtils.isEmpty(sourcePath)
		){
			Path path = Paths.get(sourcePath);

			String pageContent = result.getPageContent();
			String pageContentEtalon = result.getEtalonPageContent();

			pageContent = "STUB "  + clearResult(pageContent);
			pageContentEtalon = "NO_STUB " + clearResult(pageContentEtalon);
			String fullContent = result.getFinalUrl() + "\n" + pageContent + "\n" + pageContentEtalon + "\n\n";

			try {
				Files.write(path, fullContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new AnalysisException("Error write sources to file! " + path);
			}
		}
	}

	private String clearResult(String result){
		result = result != null ? result.replaceAll("\n", " ") : "";
		return Jsoup.parse(result).text();
	}


	private void obtainResult(AnonymizerAnalysisResult analysisResult, ExecutionAnonymizerResult executionResult) throws AnalysisException {
		analysisResult.setCheckUnit(executionResult.getCheckUnit());

		analysisResult.setHttpStatus(executionResult.getHttpStatus());
		analysisResult.setHttpStatusEtalon(executionResult.getHttpStatusEtalon());
		analysisResult.setHttpHeaders(executionResult.getHttpHeaders());
		analysisResult.setHttpHeadersEtalon(executionResult.getHttpHeadersEtalon());

		/* ERROR */

		analysisResult.setErrorCode(executionResult.getErrorCode());
		analysisResult.setScreenshot(executionResult.getScreenshot());
		analysisResult.setPageSize(sizeOf(executionResult.getPageContent()));

        analysisResult.setUseEtalon(executionResult.getUseEtalon() == null || executionResult.getUseEtalon());
        analysisResult.setEtalonErrorCode(executionResult.getEtalonErrorCode());
        analysisResult.setEtalonScreenshot(executionResult.getEtalonScreenshot());
        analysisResult.setEtalonPageSize(sizeOf(executionResult.getEtalonPageContent()));

        analysisResult.setStubUrl(executionResult.getStubUrl());
        analysisResult.setFinalUrl(executionResult.getFinalUrl());

		appendInfo(analysisResult, executionResult.getDetails());

		if (analysisResult.hasError()) {
			analysisResult.setCheckResult(
					obtainErrorResult(analysisResult));
			return;
		}

		boolean wasRedirect = false;
		if (!isEmpty(analysisResult.getFinalUrl())){
			wasRedirect = !URLUtils.simpleCompareUrls(analysisResult.getFinalUrl(), executionResult.getCheckUnit().getValue());
		}
		analysisResult.setRedirectionDetected(wasRedirect);


		// todo check that hidemyass final url equals to initial or contains in erdi

		if (analysisResult.getUseEtalon()) {

            // проверка на критическую ошибку эталона
            if (analysisResult.hasEtalonError()){
                CheckUnitJobResult errorResult = obtainErrorEtalon(analysisResult);
                if (errorResult != null){
                    analysisResult.setCheckResult(errorResult);
                    return;
                }
                else {
					// алгоритм без эталона!
				}
            }
		    else {
                try {
                    analysisResult.setSimilarityPercent(
                            AnalysisUtils.getTextSimilarityPercent(
                                    executionResult.getPageContent(),
                                    executionResult.getEtalonPageContent()));

					boolean isSimilarityEtalon = analysisResult.getSimilarityPercent() > similarityThreshold;
                    if (isSimilarityEtalon) {
                        analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
						appendInfo(analysisResult, String.format("Сходство текста %d%% (порог: %d%%).",
								analysisResult.getSimilarityPercent(), similarityThreshold));
                        return;
                    }
                    else {
						// алгоритм без эталона!
					}
                }
                catch (IOException e) {
                    String msg = "Ошибка при сверке с эталоном";
                    log.error(msg, e);
                    throw new AnalysisException(msg, e);
                }
            }
		}

		// проверка на 'пустую страницу'
		boolean isEmptyPage = analysisResult.getPageSize() < EMPTY_PAGE_SIZE;
		if (isEmptyPage) {
			appendInfo(analysisResult,
					String.format("Результат анализа контента: найдена 'пустая страница' (размер: %d байт, порог: %d байт).",
							analysisResult.getPageSize(), EMPTY_PAGE_SIZE));
		}

		NLPCategory resultNLP = getResultNLP(analysisResult.getFinalUrl(), executionResult);
		analysisResult.setResultNLP(resultNLP.getDescription());
		appendInfo(analysisResult, String.format("Результат NLP: %s.", resultNLP.getDescription()));

		if (isEmptyPage){
			analysisResult.setCheckResult(DOUBTFUL);
			return;
		}

		if (resultNLP == NLPCategory.ERROR){
			analysisResult.setCheckResult(DOUBTFUL);
			return;
		}
		if (resultNLP == NLPCategory.STUB){
			analysisResult.setCheckResult(COMPLETED);
			return;
		}
		if (resultNLP == NLPCategory.NO_STUB){
			analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
			return;
		}

		if (wasRedirect){
			// ничего. Т.к. для анонимайзера сложно/невозможно проверить редирект.
		}

		analysisResult.setCheckResult(DOUBTFUL);
	}

	private NLPCategory getResultNLP(String url, ExecutionAnonymizerResult result){
		String page = clearResult(result.getPageContent());

		log.info("Запуск NLP: " + url);
		NLPCategory nlpCategory = classificationService.classify(page, NLPModel.PAGE_CONTENT_CLASSIFICATOR);
		nlpCategory = nlpCategory == null ? NLPCategory.EXCEPTION : nlpCategory;

		log.info("Результат NLP: " + nlpCategory.getDescription());
		return nlpCategory;
	}

	private CheckUnitJobResult obtainErrorResult(AnonymizerAnalysisResult aRes) {
		String errorCode = aRes.getErrorCode();
		StringBuffer details = new StringBuffer();

		CheckUnitJobResult result = AnalysisUtils.obtainErrorResult(errorCode, details);
		appendInfo(aRes, details.toString());

		return result == null ? COMPLETED : result;
	}

    private CheckUnitJobResult obtainErrorEtalon(AnonymizerAnalysisResult aRes){
        String errorCodeEtalon = aRes.getEtalonErrorCode();
        StringBuffer details = new StringBuffer();

        CheckUnitJobResult result = AnalysisUtils.obtainErrorResultEtalon(errorCodeEtalon, details);
        appendInfo(aRes, details.toString());

        return result;
    }

	private boolean isStub(AnonymizerAnalysisResult analysisResult,
						   ExecutionAnonymizerResult executionResult)
			throws IOException {

		if (checkStubUrl(executionResult.getFinalUrl(), executionResult.getStubUrl()))
			return true;

		String content = executionResult.getPageContent();

		analysisResult.setLinkCount(AnalysisUtils.getLinkCounts(content));
		analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(content, keyWords));
		analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(analysisResult.getFinalUrl(), content));

		StringBuffer stubDetails = new StringBuffer();

		boolean res = StubAnalysis.isStub(analysisResult, StubAnalysis.getAnonymousStubWeights(), stubDetails);
		appendInfo(analysisResult, stubDetails.toString());

		return res;
	}

	private boolean checkStubUrl(String finalUrl, String stubUrl) {
		if (StringUtils.isEmpty(finalUrl) || StringUtils.isEmpty(stubUrl))
			return false;

		finalUrl = finalUrl.toLowerCase();
		stubUrl = stubUrl.toLowerCase();

		return finalUrl.contains(stubUrl) ||
				URLUtils.simpleCompareUrls(finalUrl, stubUrl);
	}

	private Integer sizeOf(String pageContent) {
		return StringUtils.isEmpty(pageContent) ? 0 : pageContent.length();
	}

	private void appendInfo(AnonymizerAnalysisResult aRes, String append){
		aRes.setStubScoreInfo(AnalysisUtils.appendString(aRes.getStubScoreInfo(), append));
	}

}

