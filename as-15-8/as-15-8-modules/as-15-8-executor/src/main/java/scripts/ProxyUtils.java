package scripts;

import org.openqa.selenium.Proxy;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProxyUtils {

    public static enum ProxyType {
        HTTP, HTTPS, SOCKS;

        public String getProxyName(){
            return this.toString().toLowerCase();
        }
        public static ProxyType getProxyType(String type, ProxyType defProxyType){
            type = (type == null ? "" : type).trim();
            for (ProxyType t : values()){
                if (t.getProxyName().equalsIgnoreCase(type))
                    return t;
            }
            return defProxyType;
        }

        public static ProxyType getProxyType(String type){
            return getProxyType(type, null);
        }
    }

    public static Proxy getSeleniumProxy(String proxy) throws IllegalArgumentException {
        if (!checkProxy(proxy)) {
            return null;
        }

        Proxy oProxy = new Proxy();
        oProxy.setProxyType(Proxy.ProxyType.MANUAL);

        ProxyType proxyType = getProxyType(proxy);
        String host = getProxyHost(proxy);
        String port = getProxyPort(proxy);
        String user = getProxyUser(proxy);
        String pass = getProxyPass(proxy);

        String simpleProxyUrl = host + ":" + port;

        if (proxyType == ProxyType.HTTP){
            oProxy.setHttpProxy(simpleProxyUrl);
            oProxy.setSslProxy(simpleProxyUrl);
        }
        else if (proxyType == ProxyType.HTTPS){
            oProxy.setHttpProxy(simpleProxyUrl);
            oProxy.setSslProxy(simpleProxyUrl);
        }
        else if (proxyType == ProxyType.SOCKS){
            throw new IllegalArgumentException("Socks проскси не должен использоваться!");

            // todo - код не работает
            /*
            oProxy.setSocksProxy(simpleProxyUrl);
            if (user != null && !user.isEmpty()){
                oProxy.setSocksUsername(user);
                oProxy.setSocksPassword(pass);
            }*/
        }
        else {
            throw new IllegalArgumentException("Не определен тип прокси: " + proxyType);
        }

        return oProxy;
    }

    public static String getFullProxy(String type, String host, String port, String user, String pass){
        if (host == null || host.isEmpty())
            return null;

        ProxyType proxyType = ProxyType.getProxyType(type, ProxyType.HTTP);

        port = port == null || port.isEmpty() ? "80" : port;
        user = user == null ? "" : user;
        pass = pass == null ? "" : pass;

        String proxy = String.format("%s://%s:%s@%s:%s", proxyType.getProxyName(), URLEncoder.encode(user), URLEncoder.encode(pass), host, port);
        return proxy;
    }

    protected static Matcher getProxyMatcher(String fullProxy){
        if (fullProxy == null || fullProxy.isEmpty())
            return null;
        Pattern pattern = Pattern.compile("^(.*?)://(.*?):(.*?)@(.*?):(.*?)$");
        Matcher matcher = pattern.matcher(fullProxy);
        if (matcher.find()){
            return matcher;
        }
        return null;
    }

    public static boolean checkProxy(String fullProxy){
        Matcher matcher = getProxyMatcher(fullProxy);
        return matcher != null;
    }

    public static ProxyType getProxyType(String fullProxy){
        Matcher matcher = getProxyMatcher(fullProxy);
        return matcher == null ? null : ProxyType.getProxyType(matcher.group(1), ProxyType.HTTP);
    }

    public static String getProxyHost(String fullProxy){
        Matcher matcher = getProxyMatcher(fullProxy);
        return matcher == null ? null : matcher.group(4);
    }

    public static String getProxyPort(String fullProxy){
        Matcher matcher = getProxyMatcher(fullProxy);
        return matcher == null ? null : matcher.group(5);
    }

    public static String getProxyUser(String fullProxy){
        Matcher matcher = getProxyMatcher(fullProxy);
        return matcher == null ? null : URLDecoder.decode(matcher.group(2));
    }

    public static String getProxyPass(String fullProxy){
        Matcher matcher = getProxyMatcher(fullProxy);
        return matcher == null ? null : URLDecoder.decode(matcher.group(3));
    }
}
