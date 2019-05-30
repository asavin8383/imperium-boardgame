package service.impl;

import analysis.AnalysisResult;
import analysis.AnalysisUtils;
import analysis.VpnAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.AnalysisException;
import enums.ArrangementUnitCheckResult;
import execution.ExecutionVpnJobResult;
import lombok.Getter;
import model.KeyWord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import service.AnalyzerService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static enums.ArrangementUnitCheckResult.*;


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

			analysisResult.setKeyWordsCount(AnalysisUtils.getCountKeyWords(pageContent, keyWords));

			analysisResult.setDomainNameCount(AnalysisUtils.getDomainCount(result.getCheckUnit().getValue(), pageContent));

			analysisResult.setSimilarityOriginPercent(AnalysisUtils.getTextSimilarityPercent(pageContent, result.getPageContentEtalon()));

			analysisResult.setLinkCount(AnalysisUtils.getLinkCounts(pageContent));
		}
	}


	protected ArrangementUnitCheckResult obtainResult(VpnAnalysisResult aRes, ExecutionVpnJobResult jobRes) {

		if (aRes.getResponseError()){
			String errorCode = aRes.getResponseErrorCode();

			if (errorCode == null || errorCode.isEmpty())
				return TIMEOUT_ERROR;

			if (errorCode.contains("TIME"))
				return TIMEOUT_ERROR;

			if (errorCode.contains("DNS"))
				return DNS_ERROR;

			if (errorCode.contains("SOCKET"))
				return SOCKET_ERROR;

			return HTTP_SERVER_SEND_NO_RESPONSE;
		}

		// todo - взять адрес VPN заглушки
		String vpnStub = "";

		// конечный URL совпадает с vpn - заглушкой
		if (AnalysisUtils.simpleCompareUrls(aRes.getPageUrlFinal(), vpnStub)){
			return COMPLETED;
		}

		// сравнение конечного и начального URL
		boolean wasRedirect = !AnalysisUtils.simpleCompareUrls(aRes.getPageUrlFinal(), jobRes.getCheckUnit().getValue());

		// сравнение контента иходника с эталоном
		if (!wasRedirect && aRes.getSimilarityOriginPercent() > 80){
			return FORBIDDEN_CONTENT_DETECTED;
		}
		if (wasRedirect && aRes.getSimilarityOriginPercent() > 90){
			return FORBIDDEN_CONTENT_DETECTED;
		}

		// веса критериев для заглушки
		int pageSizeWeight = getPageSizeWeight(aRes.getPageSize());
		int keyWordsCountWeight = getKeyWordsCountWeight(aRes.getKeyWordsCount());
		int domainCountWeight = getDomainCountWeight(aRes.getDomainNameCount());
		int linkCountWeight = getLinkCountWeight(aRes.getLinkCount());
		int diffWeight = getDiffWeight(aRes.getSimilarityOriginPercent(), aRes.getPageSize());

		// суммируем веса криетериев для определения заглушки
		int sumStubWeight =
				pageSizeWeight + keyWordsCountWeight + domainCountWeight + linkCountWeight + diffWeight;

		int maxStubWeight = getMaxWeight();
		double kWeight = (double)sumStubWeight / (double)getMaxWeight();

		System.out.println("sumStubWeight = " + sumStubWeight + ", maxWeight = " + maxStubWeight + ", k = " + kWeight);

		// процентный вес заглушки оносительно максимума
		if (kWeight >= 0.8){
			return COMPLETED;
		}

		return FORBIDDEN_CONTENT_DETECTED;
	}

	private int getPageSizeWeight(Integer size){
		size = size == null ? 0 : size;

		int maxSize = 1024;
		int minWeight = 20;
		int maxPercentWeight = 10;

		if (size > maxSize)
			return 0;

		return ((maxSize-size)/maxSize)*maxPercentWeight + minWeight;
	}

	private int getKeyWordsCountWeight(Integer count){
		count = count == null ? 0 : count;

		int minCount1 = 3;
		int minCount2 = 10;

		int minWeight1 = 15;
		int minWeight2 = 30;

		if (count < minCount1)
			return 0;

		if (count < minCount2)
			return minWeight1;

		return minWeight2;
	}

	private int getDomainCountWeight(Integer count){
		count = count == null ? 0 : count;

		int weight1 = 20;
		int weight2 = 10;
		int weight3 = 0;

		if (count == 0)
			return weight1;

		if (count <= 2)
			return weight2;

		return weight3;
	}

	private int getLinkCountWeight(Integer count){
		count = count == null ? 0 : count;

		int maxCount = 10;
		int minWeight = 10;
		int maxPercentWeight = 10;

		if (count > maxCount)
			return 0;

		return ((maxCount-count)/maxCount)*maxPercentWeight + minWeight;
	}

	private int getDiffWeight(Integer similarPercent, Integer size){
		similarPercent = similarPercent == null ? 0 : similarPercent;
		size = size == null ? 0 : size;

		int weight1 = 15;
		int weight2 = 10;

		if (size < 1024 && similarPercent < 20){
			return weight1;
		}
		else if (similarPercent < 20){
			return weight2;
		}
		return 0;
	}

	private int getMaxWeight(){
		return getPageSizeWeight(0) + getKeyWordsCountWeight(100) +
				getDomainCountWeight(0) + getLinkCountWeight(0) +
				getDiffWeight(0, 0);
	}

}
