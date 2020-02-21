package analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@UtilityClass
public class StubAnalysis {

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class StubWeights {
        public double threshold;
        public double pageSize;
        public double keyWords;
        public double domainCount;
        public double linkCount;

        public double allWeights(){
            return pageSize + keyWords + domainCount + linkCount;
        }
    }

    public static boolean isStub(StubAnalysisResult res, StubWeights weights, StringBuffer details) {

        // веса критериев для заглушки
        double pageSizePoints = weights.pageSize * getPageSizeHit(res.getPageSize());
        double keyWordsCountPoints = weights.keyWords * getKeyWordsCountHit(res.getKeyWordsCount());
        double domainCountPoints = weights.domainCount * getDomainCountHit(res.getDomainNameCount());
        double linkCountPoints = weights.linkCount * getLinkCountHit(res.getLinkCount());

        final double maxPoints = 100 * weights.allWeights();

        // суммируем веса криетериев для определения заглушки
        double sumPoints = pageSizePoints + keyWordsCountPoints + domainCountPoints + linkCountPoints;
        sumPoints = sumPoints > maxPoints ? maxPoints : sumPoints;

        double k = sumPoints / maxPoints;
        boolean result = k >= weights.threshold;

        String info = String.format("Результат анализа контента: %s (коэф = %.2f, порог = %.2f).",
                (result ? "обнаружена заглушка" : "обнаружен контент страницы"), k, weights.threshold);
        log.info(info);

        AnalysisUtils.appendString(details, info);

        // процентный вес
        return result;
    }

    public static boolean isLittleStub(StubAnalysisResult aRes, String content, StringBuffer details) {
        content = content == null ? "" : content;

        boolean res = (aRes.getPageSize() < 1024 && aRes.getDomainNameCount() <= 1 && aRes.getLinkCount() <= 2);
        if (res){
            String title = AnalysisUtils.getTitle(content);
            AnalysisUtils.appendString(details,
                    "Результат анализа контента: найдена 'пустая страница'" +
                    (StringUtils.isEmpty(title) ? "" : " с заголовком: " + title) + ".");
        }
        return res;
    }


    public static StubWeights getDefaultStubWeights(){
        return  new StubWeights(0.8, 0.25, 0.4, 0.25, 0.1);
    }

    public static StubWeights getLittleStubWeights(){
        return new StubWeights(0.7, 0.5, 0.2, 0.2, 0.1);
    }

    public static StubWeights getAnonymousStubWeights(){
        return new StubWeights(0.8, 0.35, 0.15, 0.15, 0.35);
    }


   /* private static String appendToString(String str, String append){
        return  (StringUtils.isEmpty(str) ? "" : str) +
                (StringUtils.isEmpty(str) || StringUtils.isEmpty(append) ? "" : " ") +
                (StringUtils.isEmpty(append) ? "" : append);
    }*/

    // вес от 0 о 100 (0 - большой размер, 100 - маленький)
    private static int getPageSizeHit(Integer size){
        return getPageSizeHit(size, null, null);
    }
    private static int getPageSizeHit(Integer size, Integer maxSize, Integer minPercent){
        size = size == null ? 0 : size;

        maxSize = maxSize == null ? 2048 : maxSize;
        minPercent = minPercent == null ? 70 : (minPercent < 0 ? 0 : (minPercent > 100 ? 100 : minPercent));

        if (size > maxSize)
            return 0;

        return ((maxSize-size)/maxSize)*(100-minPercent) + minPercent;
    }

    // вес от 0 до 100 (0 - мало слов, 100 - много)
    private static int getKeyWordsCountHit(Integer count){
        count = count == null ? 0 : count;

        int minCount1 = 2;
        int minCount2 = 8;
        int minCount3 = 15;

        if (count < minCount1)
            return 0;

        if (count < minCount2)
            return 50;

        count = count < minCount3 ? count : minCount3;

        return (count/minCount3) * 50 + 50;
    }

    // вес от 0 до 100 (0 - встретилось много доментов, 100 - ниодного домена)
    private static int getDomainCountHit(Integer count){
        count = count == null ? 0 : count;

        if (count == 0)
            return 100;

        if (count <= 2)
            return 50;

        return 0;
    }

    // вес от 0 до 100 (0 - встретилось много ссылок, 100 - мало ссылок)
    private static int getLinkCountHit(Integer count){
        count = count == null ? 0 : count;

        int maxCount = 10;

        if (count > maxCount)
            return 0;

        return ((maxCount-count)/maxCount)*50 + 50;
    }
}
