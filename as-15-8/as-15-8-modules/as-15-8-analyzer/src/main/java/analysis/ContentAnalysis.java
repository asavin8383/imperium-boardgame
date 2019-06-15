package analysis;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@UtilityClass
public class ContentAnalysis {

    public static final double THRESHOLD = 0.6;
    public static final double pageSize_weight = 0.15;
    public static final double domainCount_weight = 0.65;
    public static final double linkCount_weight = 0.20;

    public static boolean forbiddenContent(StubAnalysisResult res) {

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

        String info = String.format("Запрещенный сайт: %s (коэф = %.2f, порог = %.2f)", (result ? "да" : "нет"), k, THRESHOLD);
        log.info(info);

        String score = res.getStubScoreInfo();
        score = StringUtils.isEmpty(score) ? "" : score;
        score += (StringUtils.isEmpty(score) ? "" : " ") + info;
        res.setStubScoreInfo(score);

        // процентный вес
        return result;
    }

    // вес от 0 о 100 (0 - малый размер, 100 - большой)
    private static int getPageSizeHit(Integer size){
        size = size == null ? 0 : size;

        int minSize = 1024*2;
        int maxSize = 1024*20;

        if (size < minSize)
            return 0;

        size = (size > maxSize ? maxSize : size);

        return (size/maxSize)*40 + 60;
    }

    // вес от 0 до 100 (0 - ниодного домена, 100 - много доменов)
    private static int getDomainCountHit(Integer count){
        count = count == null ? 0 : count;

        int max = 40;

        if (count == 0)
            return 0;

        if (count <= 2)
            return 50;

        count = (count > max ? max : count);

        return (count/max)*50 + 50;
    }

    // вес от 0 до 100 (0 - мало, 100 - много ссылок)
    private static int getLinkCountHit(Integer count){
        count = count == null ? 0 : count;

        int min = 10;
        int max = 100;

        if (count < min)
            return 0;

        count = (count > max ? max : count);

        return (count/max)*50 + 50;
    }
}
