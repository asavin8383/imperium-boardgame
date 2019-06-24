package analysis;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ContentAnalysis {

    public static final double THRESHOLD = 0.6;
    public static final double pageSize_weight = 0.20;
    public static final double domainCount_weight = 0.50;
    public static final double linkCount_weight = 0.30;

    public static boolean forbiddenContent(StubAnalysisResult res, StringBuffer details) {

        // суммп попадиний
        double pageSize_points = pageSize_weight * getPageSizeHit(res.getPageSize());
        double domainCount_points = domainCount_weight * getDomainCountHit(res.getDomainNameCount());
        double linkCount_points = linkCount_weight * getLinkCountHit(res.getLinkCount());

        double allWeights = pageSize_weight + domainCount_weight + linkCount_weight;
        final double maxPoints = 100 * allWeights;

        // суммируем веса криетериев для определения заглушки
        double sumPoints = pageSize_points + domainCount_points + linkCount_points;
        sumPoints = sumPoints > maxPoints ? maxPoints : sumPoints;

        double k = sumPoints / maxPoints;
        boolean result = k >= THRESHOLD;

        String info = String.format("Запрещенный сайт: %s (коэф = %.2f, порог = %.2f).", (result ? "да" : "нет"), k, THRESHOLD);
        log.info(info);

        AnalysisUtils.appendString(details, info);

        // процентный вес
        return result;
    }

    // вес от 0 о 100 (0 - малый размер, 100 - большой)
    private static int getPageSizeHit(Integer size){
        size = size == null ? 0 : size;

        int minSize = 1024*2;
        int maxSize1 = 1024*20;
        int maxSize2 = 1024*100;

        if (size < minSize)
            return 0;
        else if (size < maxSize1){
            return (size/maxSize1)*30 + 40;
        }
        else if (size < maxSize2){
            return (size/maxSize2)*30 + 70;
        }
        return 100;
    }

    // вес от 0 до 100 (0 - ниодного домена, 100 - много доменов)
    private static int getDomainCountHit(Integer count){
        count = count == null ? 0 : count;

        int min = 2;
        int max = 20;

        if (count < min)
            return 0;
        else if (count < max)
            return (count/max)*50 + 50;
        return 100;
    }

    // вес от 0 до 100 (0 - мало, 100 - много ссылок)
    private static int getLinkCountHit(Integer count){
        count = count == null ? 0 : count;

        int min = 2;
        int max1 = 5;
        int max2 = 20;

        if (count < min)
            return 0;
        if (count < max1)
            return (count/max1)*10 + 20;
        else if (count < max2)
            return (count/max2)*70 + 30;
        return 100;
    }
}
