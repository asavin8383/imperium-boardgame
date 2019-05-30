package analysis;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import model.KeyWord;

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

    public static int getDomainCount(String url, String text) throws MalformedURLException {
        String domain = getDomain(url);
        return countMatches(text, domain);
    }

    public static String getDomain(String url) throws MalformedURLException {
        if (url == null || url.trim().isEmpty()){
            return null;
        }
        URL aURL = new URL(url);
        return aURL.getHost();
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

            String path1 = u1.getPath();
            path1 = path1.endsWith("/") ? "" : path1;

            String path2 = u2.getPath();
            path2 = path2.endsWith("/") ? "" : path2;

            return u1.getHost().equals(u2.getHost()) && path1.equals(path2);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
