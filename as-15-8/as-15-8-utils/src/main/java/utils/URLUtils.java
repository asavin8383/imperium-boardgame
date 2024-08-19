package utils;

import static org.springframework.util.StringUtils.isEmpty;


public class URLUtils {

    public static boolean simpleCompareUrls(String url1, String url2) {
        if (isEmpty(url1) || isEmpty(url2))
            return false;

        try {
            URLComponent comp1 = URLComponent.fromString(url1);
            URLComponent comp2 = URLComponent.fromString(url2);

            if (comp1.toStringWithoutScheme().equals(comp2.toStringWithoutScheme()))
                return true;

            URLComponent decode1 = URLComponent.getDecodedFrom(comp1);
            URLComponent encode1 = URLComponent.getEncodedFrom(comp1);

            URLComponent decode2 = URLComponent.getDecodedFrom(comp2);
            URLComponent encode2 = URLComponent.getEncodedFrom(comp2);

            String decode1WithoutScheme = removeDomainPrefix(decode1.toStringWithoutScheme());
            String decode2WithoutScheme = removeDomainPrefix(decode2.toStringWithoutScheme());
            String encode1WithoutScheme = removeDomainPrefix(encode1.toStringWithoutScheme());
            String encode2WithoutScheme = removeDomainPrefix(encode2.toStringWithoutScheme());

            return decode1WithoutScheme.equals(decode2WithoutScheme) ||
                    encode1WithoutScheme.equals(encode2WithoutScheme);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean compareDomainsInUrls(String url1, String url2) {
        if (isEmpty(url1) || isEmpty(url2))
            return false;

        try {
            URLComponent comp1 = URLComponent.fromString(url1);
            URLComponent comp2 = URLComponent.fromString(url2);

            return comp1.getDecodedHost().equals(comp2.getDecodedHost()) ||
                    comp1.getEncodedHost().equals(comp2.getEncodedHost());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String removeDomainPrefix(String url) {
        return url.replaceFirst("^(www\\.wap\\.|www\\.|wap\\.)", "");
    }
}
