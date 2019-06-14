package analysis;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@UtilityClass
public class StubAnalysis {

    public static final double THRESHOLD = 0.8;
    public static final double pageSize_percent = 0.25;
    public static final double keyWords_percent = 0.4;
    public static final double domainCount_percent = 0.25;
    public static final double linkCount_percent = 0.1;

    public static boolean isStub(StubAnalysisResult result) {
        return isStub(result,
                pageSize_percent, keyWords_percent,
                domainCount_percent, linkCount_percent);
    }

    public static boolean isStub(StubAnalysisResult res,
                                 double pageSize_percent,
                                 double keyWords_percent,
                                 double domainCount_percent,
                                 double linkCount_percent) {
        double sun_percent = pageSize_percent + keyWords_percent + domainCount_percent + linkCount_percent;

        // веса критериев для заглушки
        double pageSizeWeight = pageSize_percent * getPageSizeWeight(res.getPageSize());
        double keyWordsCountWeight = keyWords_percent * getKeyWordsCountWeight(res.getKeyWordsCount());
        double domainCountWeight = domainCount_percent * getDomainCountWeight(res.getDomainNameCount());
        double linkCountWeight = linkCount_percent * getLinkCountWeight(res.getLinkCount());

        final double maxWeight = 100*sun_percent;

        // суммируем веса криетериев для определения заглушки
        double sumWeight = pageSizeWeight + keyWordsCountWeight + domainCountWeight + linkCountWeight;
        sumWeight = sumWeight > maxWeight ? maxWeight : sumWeight;

        double kWeight = sumWeight / maxWeight;
        boolean result = kWeight >= THRESHOLD;

        String info = String.format("Заглушка: %s (коэф = %.2f, порог = %.2f)", (result ? "да" : "нет"), kWeight, THRESHOLD);
        log.info(info);

        String score = res.getStubScoreInfo();
        score = StringUtils.isEmpty(score) ? "" : score;
        score += (StringUtils.isEmpty(score) ? "" : " ") + info;
        res.setStubScoreInfo(score);

        // процентный вес
        return result;
    }

    public boolean checkStubUrl(String url, String stubUrl){
        url = url != null ? url : "";
        stubUrl = stubUrl != null ? stubUrl : "";

        boolean res1 = AnalysisUtils.simpleCompareUrls(url, stubUrl);
        boolean res2 = url.toLowerCase().contains(stubUrl.toLowerCase());
        return res1 || res2;
    }

    // вес от 0 о 100 (0 - большой размер, 100 - маленький)
    private static int getPageSizeWeight(Integer size){
        size = size == null ? 0 : size;

        int maxSize = 2048;

        if (size > maxSize)
            return 0;

        return ((maxSize-size)/maxSize)*50 + 50;
    }

    // вес от 0 до 100 (0 - мало слов, 100 - много)
    private static int getKeyWordsCountWeight(Integer count){
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
    private static int getDomainCountWeight(Integer count){
        count = count == null ? 0 : count;

        if (count == 0)
            return 100;

        if (count <= 2)
            return 50;

        return 0;
    }

    // вес от 0 до 100 (0 - встретилось много ссылок, 100 - мало ссылок)
    private static int getLinkCountWeight(Integer count){
        count = count == null ? 0 : count;

        int maxCount = 10;

        if (count > maxCount)
            return 0;

        return ((maxCount-count)/maxCount)*50 + 50;
    }
}
