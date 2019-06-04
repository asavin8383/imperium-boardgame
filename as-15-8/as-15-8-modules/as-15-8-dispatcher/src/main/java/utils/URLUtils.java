package utils;

import org.springframework.web.util.UriUtils;
import java.net.URI;
import java.net.URISyntaxException;


public class URLUtils {

    public static boolean compareUrl(String u1, String u2) throws URISyntaxException {
        String norm1 = normalizeUrl(u1);
        String norm2 = normalizeUrl(u2);
        return norm1.equals(norm2);
    }

    public static String normalizeUrl(String url) throws URISyntaxException {
        url = url == null ? "" : url;
        url = url.trim();
        if (url.isEmpty())
            return url;

        url = UriUtils.encodeFragment(UriUtils.decode(url, "UTF-8"), "UTF-8");
        URI uri = new URI(url);
        URI normalize = uri.normalize();
        String res = normalize.toString();
        res = res.replaceAll("/$", "");
        return res;
    }

}
