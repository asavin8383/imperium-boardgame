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
		Boolean responseError = result.getResponseError();
		String  pageContent = result.getPageContent();
		if (pageContent == null)
			pageContent = "";
		String pageContentEtalon = result.getPageContentEtalon();
		if (pageContentEtalon == null)
			pageContentEtalon = "";

		analysisResult.setResponseError(responseError);
		analysisResult.setResponseErrorCode(chromeErrorCode);

		if (!responseError) {
			analysisResult.setPageSize(pageContent.length());
			analysisResult.setPageSizeEtalon(pageContentEtalon.length());
			analysisResult.setPageUrlFinal(result.getFinalUrlPage());
			analysisResult.setStubUrl(result.getStubUrl());

			analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(pageContent, keyWords));

			analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(result.getCheckUnit().getValue(), pageContent));

			analysisResult.setSimilarityOriginPercent(AnalysisUtils.getTextSimilarityPercent(pageContent, result.getPageContentEtalon()));

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
		if (checkStubUrl(aRes.getPageUrlFinal(), aRes.getStubUrl())){
			return COMPLETED;
		}

		// сравнение конечного и начального URL
		boolean wasRedirect = !AnalysisUtils.simpleCompareUrls(aRes.getPageUrlFinal(), jobRes.getCheckUnit().getValue());

		// сравнение контента иходника с эталоном
		if (aRes.getSimilarityOriginPercent() >= 90){
			return FORBIDDEN_CONTENT_DETECTED;
		}

		double pageSize_percent = 0.25;
		double keyWords_percent = 0.4;
		double domainCount_percent = 0.25;
		double linkCount_percent = 0.1;
		double sun_percent = pageSize_percent + keyWords_percent + domainCount_percent + linkCount_percent;

		// веса критериев для заглушки
		double pageSizeWeight = pageSize_percent * getPageSizeWeight(aRes.getPageSize());
		double keyWordsCountWeight = keyWords_percent * getKeyWordsCountWeight(aRes.getKeyWordsCount());
		double domainCountWeight = domainCount_percent * getDomainCountWeight(aRes.getDomainNameCount());
		double linkCountWeight = linkCount_percent * getLinkCountWeight(aRes.getLinkCount());

		final double maxWeight = 100*sun_percent;
		final double kStub = 0.8;

		// суммируем веса криетериев для определения заглушки
		double sumWeight = pageSizeWeight + keyWordsCountWeight + domainCountWeight + linkCountWeight;
		sumWeight = sumWeight > maxWeight ? maxWeight : sumWeight;

		double kWeight = sumWeight / maxWeight;

		log.info("kWeight = " + kWeight + " | kStub = " + kStub);

		aRes.setStubScoreInfo(String.format("k = %.2f (stub >= %.2f)", kWeight, kStub));

		// процентный вес заглушки оносительно максимума
		if (kWeight >= kStub){
			return COMPLETED;
		}

		// todo - проверить конечную ссылку на запрещенную (завести статус??? для того чтобы в дальнейшем проверять пачкой)
		if (wasRedirect){
		}

		return FORBIDDEN_CONTENT_DETECTED;
	}

	private boolean checkStubUrl(String url, String stubUrl){
		url = url != null ? url : "";
		stubUrl = stubUrl != null ? stubUrl : "";

		boolean res1 = AnalysisUtils.simpleCompareUrls(url, stubUrl);
		boolean res2 = url.toLowerCase().contains(stubUrl.toLowerCase());
		return res1 || res2;
	}

	// вес от 0 о 100 (0 - большой размер, 100 - маленький)
	private int getPageSizeWeight(Integer size){
		size = size == null ? 0 : size;

		int maxSize = 2048;

		if (size > maxSize)
			return 0;

		return ((maxSize-size)/maxSize)*50 + 50;
	}

	// вес от 0 до 100 (0 - мало слов, 100 - много)
	private int getKeyWordsCountWeight(Integer count){
		count = count == null ? 0 : count;

		int minCount1 = 3;
		int minCount2 = 10;
		int minCount3 = 30;

		if (count < minCount1)
			return 0;

		if (count < minCount2)
			return 50;

		count = count < minCount3 ? count : minCount3;

		return (count/minCount3) * 50 + 50;
	}

	// вес от 0 до 100 (0 - встретилось много доментов, 100 - ниодного домена)
	private int getDomainCountWeight(Integer count){
		count = count == null ? 0 : count;

		if (count == 0)
			return 100;

		if (count <= 2)
			return 50;

		return 0;
	}

	// вес от 0 до 100 (0 - встретилось много ссылок, 100 - мало ссылок)
	private int getLinkCountWeight(Integer count){
		count = count == null ? 0 : count;

		int maxCount = 10;

		if (count > maxCount)
			return 0;

		return ((maxCount-count)/maxCount)*50 + 50;
	}

}
