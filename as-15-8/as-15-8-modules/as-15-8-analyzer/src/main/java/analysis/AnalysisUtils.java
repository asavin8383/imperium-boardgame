package analysis;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import enums.CheckUnitJobResult;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import model.KeyWord;
import org.springframework.web.util.UriUtils;

import static enums.CheckUnitJobResult.*;
import static enums.CheckUnitJobResult.COMPLETED;
import static java.net.IDN.toASCII;
import static java.net.IDN.toUnicode;
import static org.springframework.util.StringUtils.isEmpty;

public class AnalysisUtils {

    public static final String UTF8 =  StandardCharsets.UTF_8.name();

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
        try {
            String domain = getDomain(url);
            return countMatches(text, domain);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDomain(String url) throws URISyntaxException {
        if (url == null || url.trim().isEmpty()){
            return null;
        }
        URI u = new URI(url);
        return u.getHost();
    }

    public static int getLinkCounts(String text){
        if (text == null)
            return 0;
        Document doc = Jsoup.parse(text);
        Elements links = doc.select("a[href]");
        return links.size();
    }

    public static boolean simpleCompareUrls(String url1, String url2) {
        url1 = url1 == null ? "" : url1.trim();
        url2 = url2 == null ? "" : url2.trim();

        if (url1.isEmpty() && url2.isEmpty())
            return false;

        if (!url1.startsWith("http"))
            url1 = "http://" + url1;
        if (!url2.startsWith("http"))
            url2 = "http://" + url2;

        try {
            URL u1 = new URL(url1);
            URL u2 = new URL(url2);

            String host1 = u1.getHost() == null ? "" : u1.getHost();
            String host2 = u2.getHost() == null ? "" : u2.getHost();
            boolean hostsEquals = host1.equals(host2) || host1.equals(toUnicode(host2)) ||  host1.equals(toASCII(host2));

            String path1 = u1.getPath();
            path1 = path1 == null ? "" : path1;
            path1 += path1.endsWith("/") ? "" : "/";

            String path2 = u2.getPath();
            path2 = path2 == null ? "" : path2;
            path2 += path2.endsWith("/") ? "" : "/";

            boolean pathsEquals = path1.equals(path2) ||
                    path1.equals(UriUtils.decode(path2, UTF8)) ||
                    path1.equals(UriUtils.encodeFragment(UriUtils.decode(path2, UTF8), UTF8));

            return hostsEquals && pathsEquals;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static CheckUnitJobResult obtainErrorResult(String errorCode, StringBuffer details){
        details = details == null ? new StringBuffer() : details;
        CheckUnitJobResult result = null;
        String message = null;

        if (!isEmpty(errorCode)){
            if (errorCode.contains(INTERNAL_ERROR.name())) {
                message = "Ошибка! " + errorCode;
                result = INTERNAL_ERROR;
            }
            else if (errorCode.contains("NO_INTERNET")) {
                message = "Ошибка! Нет интернета! " + errorCode;
                result = INTERNAL_ERROR;
            }
            else if (errorCode.contains("ERR_PROXY")) {
                message = "Ошибка доступа к прокси! " + errorCode;
                result = INTERNAL_ERROR;
            }
            else if (errorCode.contains("SOCKET")){
                message = "Ошибка доступа к ресурсу: " + errorCode + ". Есть вероятность проблемы с сетью.";
                result = DOUBTFUL;
            }
            else if (errorCode.contains("TIME_OUT_CHECKING")) {
                message = "Ошибка таймаута при проверке браузера: " + errorCode + ". Возможно ресурс доступен.";
                result = DOUBTFUL;
            }
            else if (errorCode.contains("TIMEOUT") || errorCode.contains("TIME_OUT")) {
                message = "Ошибка таймаута: " + errorCode + ". Ресурс вероятно недоступен.";
                result = COMPLETED;
            }
            else if (errorCode.contains("DNS")){
            }

            if (result == null){
                message = "Ошибка доступа к ресурсу: " + errorCode;
                result = COMPLETED;
            }

            details.append(message);
            return result;
        }
        return null;
    }

    public static CheckUnitJobResult obtainErrorResultEtalon(String errorCode, StringBuffer details){
        details = details == null ? new StringBuffer() : details;
        CheckUnitJobResult result = null;
        String message = null;

        if (!isEmpty(errorCode)){
            if (errorCode.contains(INTERNAL_ERROR.name())) {
                message = "Ошибка! " + errorCode;
                result = INTERNAL_ERROR;
            }
            else if (errorCode.contains("NO_INTERNET")) {
                message = "Ошибка ЭТАЛОНА - нет интернета! " + errorCode;
                result = INTERNAL_ERROR;
            }
            else if (errorCode.contains("ERR_PROXY")) {
                message = "Ошибка ЭТАЛОНА - доступ к прокси! " + errorCode;
                result = INTERNAL_ERROR;
            }
            else {
                message = "Ошибка ЭТАЛОНА: " + errorCode + ". Не критическая";
            }

            details.append(message);
            return result;
        }
        return null;
    }

}
