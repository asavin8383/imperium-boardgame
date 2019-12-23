package robots.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.springframework.util.StringUtils;

import checkUnits.CheckUnit;
import lombok.SneakyThrows;

public interface EqualityTest {

    boolean equalTo(String found) throws MalformedURLException;

    @SneakyThrows
    static String decode(String url) {
        if (!StringUtils.isEmpty(url)) {
            return URLDecoder.decode(url,
                    StandardCharsets.UTF_8.name());
        }
        return url;
    }

    @Nullable
    static URL toUrl(String url) throws MalformedURLException {
        if (!StringUtils.isEmpty(url)) {
            return new URL(decode(url));
        }
        return null;
    }

    static EqualityTest forCheckUnit(CheckUnit unit) {
        switch (unit.getType()) {

            case DOMAIN_MASK:
            case IP_V4_SUBNET:
            case IP_V6_SUBNET:
                throw new IllegalArgumentException(
                        "Link comparison not implemented for type=" +
                                unit.getType().toString());

            case URL:
                return new UrlEquality(unit.getValue());
            case IP_V6:
                return new IPv6HostEquality(unit.getValue());
            case DOMAIN:
            default:
                return new HostEquality(unit.getValue());
        }
    }
}

class HostEquality implements EqualityTest {

    private String forbiddenHost;

    HostEquality(String forbiddenHost) {
        this.forbiddenHost = forbiddenHost;
    }

    @Override
    public boolean equalTo(String found) throws MalformedURLException {
        URL foundUrl = EqualityTest.toUrl(found);
        return foundUrl != null &&
                foundUrl.getHost().endsWith(forbiddenHost);
    }
}

class UrlEquality implements EqualityTest {

    private String originalUrl;
    private String decodedUrl;
    private boolean isEncoded;

    UrlEquality(String forbiddenUrl) {
        this.originalUrl = removeSlash(forbiddenUrl);
        this.decodedUrl = EqualityTest.decode(originalUrl);
        this.isEncoded = originalUrl.compareTo(decodedUrl) != 0;
    }

    @Override
    public boolean equalTo(String foundUrl) {
        if (StringUtils.isEmpty(foundUrl))
            return false;

        foundUrl = removeSlash(foundUrl);

        if (isEncoded)
            return originalUrl.equals(foundUrl);

        foundUrl = EqualityTest.decode(foundUrl);
        return decodedUrl.equals(foundUrl);
    }

    private String removeSlash(String url) {
        if (url != null && url.length() > 0 &&
                url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}

class IPv6HostEquality implements EqualityTest {

    private String forbiddenHost;

    IPv6HostEquality(String forbiddenHost) {
        this.forbiddenHost = removeBrackets(forbiddenHost);
    }

    @Override
    public boolean equalTo(String found) throws MalformedURLException {
        URL foundUrl = EqualityTest.toUrl(found);
        if (foundUrl != null) {
            String foundHost = removeBrackets(foundUrl.getHost());
            return forbiddenHost.equals(foundHost);
        }
        return false;
    }

    private String removeBrackets(String host) {
        return host.replaceAll("[\\[\\]]", "");
    }
}