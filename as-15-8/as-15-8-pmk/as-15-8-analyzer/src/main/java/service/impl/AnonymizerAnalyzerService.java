package service.impl;

import analysis.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionAnonymizerResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import model.NLPCategory;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import service.AnalyzerService;
import service.ClassificationService;

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

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	@Qualifier("openNLPClassificator")
	private ClassificationService classificationService;

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
		analysisResult.setJobID(executionResult.getJobID());

		obtainResult(analysisResult, executionResult);
		obtainResultNLP(analysisResult, executionResult);
		saveSources(analysisResult, executionResult);

		return analysisResult;
	}

	protected void saveSources(AnonymizerAnalysisResult analysisResult, ExecutionAnonymizerResult result){

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


	protected AnalysisResult obtainResult(AnonymizerAnalysisResult analysisResult, ExecutionAnonymizerResult executionResult) throws AnalysisException {
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
			return analysisResult;
		}

		boolean wasRedirect = false;
		if (!isEmpty(analysisResult.getFinalUrl())){
			wasRedirect = !AnalysisUtils.simpleCompareUrls(analysisResult.getFinalUrl(), executionResult.getCheckUnit().getValue());
		}
		analysisResult.setRedirectionDetected(wasRedirect);

		if (analysisResult.getPageSize() < EMPTY_PAGE_SIZE) {
			analysisResult.setCheckResult(DOUBTFUL);	// todo - так сказали делать
			appendInfo(analysisResult, "Пустая страница: размер <" + EMPTY_PAGE_SIZE + " байт.");
			return analysisResult;
		}

		// todo check that hidemyass final url equals to initial or contains in erdi

		/* ETALON */

        if ( analysisResult.getUseEtalon() ) {

            // проверка на критическую ошибку эталона
            if (analysisResult.hasEtalonError()){
                CheckUnitJobResult errorResult = obtainErrorEtalon(analysisResult);
                if (errorResult != null){
                    analysisResult.setCheckResult(errorResult);
                    return analysisResult;
                }
            }

		    if (!analysisResult.hasEtalonError()){
                try {
                    analysisResult.setSimilarityPercent(
                            AnalysisUtils.getTextSimilarityPercent(
                                    executionResult.getPageContent(),
                                    executionResult.getEtalonPageContent()));

                    if (analysisResult.getSimilarityPercent() > similarityThreshold) {
                        analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
                        appendInfo(analysisResult, "Порог сходства текста >= " + similarityThreshold + "%.");
                        return analysisResult;
                    }

                } catch (IOException e) {
                    String msg = "Ошибка при сверке с эталоном";
                    log.error(msg, e);
                    throw new AnalysisException(msg, e);
                }
            }
		}

		/* STUB */

		try {
			if (isStub(analysisResult, executionResult)) {
				analysisResult.setCheckResult(DOUBTFUL);	// todo - так сказали делать
				return analysisResult;
			}
		} catch (IOException e) {
			String msg = "Ошибка при проверке заглушки";
			log.error(msg, e);
			throw new AnalysisException(msg, e);
		}

		// проверка без эталона
		if (!wasRedirect){
			StringBuffer contentDetails = new StringBuffer();
			boolean isForbidden = ContentAnalysis.forbiddenContent(analysisResult, contentDetails);
			appendInfo(analysisResult, contentDetails.toString());
			if (isForbidden){
				analysisResult.setCheckResult(FORBIDDEN_CONTENT_DETECTED);
				return analysisResult;
			}
		}

		analysisResult.setCheckResult(DOUBTFUL);
		return analysisResult;
	}

	protected void obtainResultNLP(AnonymizerAnalysisResult analysisResult, ExecutionAnonymizerResult result){
		String page = clearResult(result.getPageContent());

		NLPCategory nlpCategory = classificationService.classify(page);
		nlpCategory = nlpCategory == null ? NLPCategory.EXCEPTION : nlpCategory;

		analysisResult.setResultNLP(nlpCategory.getDescription());
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
		String checkValue = executionResult.getCheckUnit().getValue();

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
				AnalysisUtils.simpleCompareUrls(finalUrl, stubUrl);
	}

	private Integer sizeOf(String pageContent) {
		return StringUtils.isEmpty(pageContent) ? 0 : pageContent.length();
	}

	private void appendInfo(AnonymizerAnalysisResult aRes, String append){
		aRes.setStubScoreInfo(AnalysisUtils.appendString(aRes.getStubScoreInfo(), append));
	}

}

