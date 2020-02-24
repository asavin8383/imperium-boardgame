package analysis;

import enums.CheckUnitJobResult;
import lombok.extern.slf4j.Slf4j;
import model.KeyWord;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.URLComponent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import static enums.CheckUnitJobResult.*;
import static org.springframework.util.StringUtils.isEmpty;


@Slf4j
public class AnalysisUtils {

    public static int getTextSimilarityPercent(String t1, String t2) throws IOException {
        CosineDocumentSimilarity cos = new CosineDocumentSimilarity(t1, t2);
        double similarity = cos.getCosineSimilarityAvg();
        return (int)Math.round(similarity * 100);
    }

    public static int getCountKeyWords(String text, List<KeyWord> keyWords) throws IOException {
        return KeyWordsCounter.getCount(text, keyWords);
    }

    public static int countMatches(String source, String substr){
        return StringUtils.countMatches(source, substr);
    }

    public static int getDomainCount(String url, String text) {
        URLComponent comp;
        try {
            comp = URLComponent.fromString(url);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return 0;
        }
        String domain1 = comp.getDecodedHost();
        String domain2 = comp.getEncodedHost();

        int count1 = 0;
        int count2 = 0;

        if (!StringUtils.isEmpty(domain1)){
            count1 = countMatches(text, domain1);
        }
        if (!StringUtils.isEmpty(domain2) && !domain2.equals(domain1)){
            count2 = countMatches(text, domain2);
        }
        return count1 + count2;
    }

    public static int getLinkCounts(String text){
        if (text == null)
            return 0;
        Document doc = Jsoup.parse(text);
        Elements links = doc.select("a[href]");
        return links.size();
    }

    public static String getTitle(String text){
        if (text == null)
            return null;
        Document doc = Jsoup.parse(text);
        Elements title = doc.select("title");
        return title.text();
    }

    public static String appendString(String str, String append){
        return appendString(str, append, " ");
    }
    public static String appendString(String str, String append, String delim){
        return  (StringUtils.isEmpty(str) ? "" : str) +
                (StringUtils.isEmpty(str) || StringUtils.isEmpty(append) ? "" : (delim == null ? "" : delim)) +
                (StringUtils.isEmpty(append) ? "" : append);
    }
    public static void appendString(StringBuffer strBuffer, String append){
        appendString(strBuffer, append, " ");
    }
    public static void appendString(StringBuffer strBuffer, String append, String delim){
        if (strBuffer != null && !StringUtils.isEmpty(append)){
            strBuffer.append(delim == null ? "" : delim);
            strBuffer.append(append);
        }
    }

    public static CheckUnitJobResult obtainErrorResult(String errorCode, StringBuffer details){
        return obtainErrorResultFull(errorCode, details);
    }

    public static CheckUnitJobResult obtainErrorResultEtalon(String errorCode, StringBuffer details){
        String ETALON = "[Эталон] ";
        StringBuffer detailsError = new StringBuffer();
        CheckUnitJobResult result = obtainErrorResultFull(errorCode, detailsError);

        if (result == INTERNAL_ERROR){
            details.append(ETALON);
            details.append(detailsError);
        }
        else {
            String message = String.format("Не критическая ошибка: %s", errorCode);
            details.append(ETALON);
            details.append(message);
            result = null;
        }
        return result;
    }

    public static CheckUnitJobResult obtainErrorResultFull(String errorCode, StringBuffer details){
        if (isEmpty(errorCode))
            return null;

        details = details == null ? new StringBuffer() : details;

        CheckUnitJobResult result = null;
        String message = null;

        if (errorCode.contains(INTERNAL_ERROR.name())) {
            message = String.format("Внутренняя ошибка робота: %s.", errorCode);
            result = INTERNAL_ERROR;
        }
        else if (errorCode.contains("NO_INTERNET")) {
            message = String.format("Ошибка доступа к сети: нет интернета: %s.", errorCode);
            result = INTERNAL_ERROR;
        }
        else if (errorCode.contains("ERR_PROXY")) {
            message = String.format("Ошибка доступа к ПСД: %s.", errorCode);
            result = INTERNAL_ERROR;
        }
        else if (errorCode.contains("SOCKET")){
            message = String.format("Ошибка протокола связи: %s.", errorCode);
            result = DOUBTFUL;
        }
        else if (errorCode.contains("CAPTCHA")){
            message = String.format("Обнаружена капча: %s.", errorCode);
            result = DOUBTFUL;
        }
        else if (errorCode.contains("TIME_OUT_CHECKING")) {
            message = "Превышено время ожидания при проверке браузера.";
            result = DOUBTFUL;
        }
        else if (errorCode.contains("TIMEOUT") || errorCode.contains("TIME_OUT")) {
            message = String.format("Превышено время ожидания ответа ресурса: %s", errorCode);
            result = DOUBTFUL;
        }
        else if (errorCode.contains("DNS")){
            message = String.format("Ошибка DNS сервера: %s", errorCode);
            result = COMPLETED;
        }

        if (result == null){
            message = String.format("Внешняя ошибка: %s", errorCode);
            result = DOUBTFUL;
        }

        details.append(message);
        return result;
    }

}
