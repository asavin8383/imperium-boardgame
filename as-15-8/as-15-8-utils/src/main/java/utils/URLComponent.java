package utils;

import lombok.NonNull;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class URLComponent {

    private String origUrl;
    private URL u;

    public static final String DEF_PROTOCOL = "http";
    public static final String UTF8 = "UTF-8";

    private URLComponent(){}


    @Override
    public String toString(){
        return toString(true, true, true, true);
    }

    public String toStringWithoutScheme(){
        return toString(false, true, true, true);
    }

    public String toString(boolean isScheme, boolean isHost, boolean isPort, boolean isFragment){
        String res = "";
        if (isScheme)
            res += u.getProtocol() + "://";
        if (isHost)
            res += u.getHost();
        if (isPort)
            res += (u.getPort() > 0 ? ":" + u.getPort() : "");
        if (isFragment)
            res += getFullFragment();
        return res;
    }

    public static URLComponent getDecodedFrom(@NonNull URLComponent component) throws MalformedURLException {
        URL u = component.u;
        String newHost = decodeHost(u.getHost());
        String newFragment = decodeFragment(component.getFullFragment());
        String newUrl = createNewURLString(u.getProtocol(), newHost, u.getPort(), newFragment);
        return fromString(newUrl);
    }

    public static URLComponent getEncodedFrom(@NonNull URLComponent component) throws MalformedURLException {
        URL u = component.u;
        String newHost = encodeHost(u.getHost());

        String fragment = component.getFullFragment();
        String newFragment = isDecodedFragment(fragment) ? encodeFragment(component.getFullFragment()) : fragment;
        String newUrl = createNewURLString(u.getProtocol(), newHost, u.getPort(), newFragment);

        return fromString(newUrl);
    }


    public static URLComponent fromStringNull(@NonNull String url) {
        try {
            return fromString(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }
    public static URLComponent fromString(@NonNull String url) throws MalformedURLException {
        if (!url.matches("^\\w+://.*$")){
            url = "http://" + url;
        }
        URL u = new URL(url);

        URLComponent uComp = new URLComponent();
        uComp.origUrl = url;
        uComp.u = u;

        return uComp;
    }

    public String getHost() {
        return u.getHost();
    }

    public String getDecodedHost() {
        return decodeHost(u.getHost());
    }
    public String getEncodedHost() {
        return encodeHost(u.getHost());
    }

    public boolean isSimpleHost() {
        String host = u.getHost();
        String hDecoded = IDN.toUnicode(host);
        String hEncoded = Arrays.stream(host.split("\\.")).map(IDN::toASCII).collect(Collectors.joining("."));
        return host.equals(hDecoded) && host.equals(hEncoded);
    }

    public boolean isSimpleFragment() {
        String fragment = getFullFragment();
        return fragment.equals(decodeFragment(fragment)) && fragment.equals(encodeFragment(fragment));
    }

    private static URL createNewURL(String protocol, String host, int port, String fragment) throws MalformedURLException {
        String str = createNewURLString(protocol, host, port, fragment);
        return new URL(str);
    }

    private static String createNewURLString(String protocol,  String host, int port, String fragment) {
        return (!StringUtils.isEmpty(protocol) ? protocol : DEF_PROTOCOL) +  "://" +
                (!StringUtils.isEmpty(host) ? host : "") +
                (port > 0 ? ":" + port : "") +
                fragment;
    }

    public static boolean testUrl(String str) {
        boolean res = false;
        try {
            res = testUrl(str, false);
        } catch (Exception e) {}
        return res;
    }
    public static boolean testUrl(String str, boolean exception) throws Exception {
        try {
            URLComponent comp = URLComponent.fromString(str);
            URL u = comp.u;
            String host = u.getHost();
            String fragment = comp.getFullFragment();

            decodeHost(host);           // корректность формата закодированного хоста
            encodeHost(host);           // корректность длины хоста. Возможно не удасться закодировать длинные хосты
            decodeFragment(fragment);   // проверяем только декодирование. С закодирование проблем не бывает.

            return true;
        }
        catch(Exception e){
            if (exception)
                throw e;
            return false;
        }
    }

    public boolean equals(URLComponent comp) {
        if (comp == null)
            return false;
        return this.toString().equals(comp.toString());
    }


    public boolean isIp() {
        return isIp4() || isIp6();
    }

    public boolean isIp4() {
        return InetAddressValidator.getInstance().isValidInet4Address(u.getHost());
    }

    public boolean isIp6() {
        return InetAddressValidator.getInstance().isValidInet6Address(u.getHost());
    }

    public boolean isDomain() {
        return !isIp();
    }

    public boolean isDomainMask() {
        return isDomain() && u.getHost().contains("*");
    }

    public boolean isUrl() {
        String fragment = getFullFragment();
        return !StringUtils.isEmpty(fragment) && !fragment.equals("/");
    }

    public String getFullFragment() {
        return getFullFragmentFromUrl(u, StringUtils.isEmpty(origUrl) ? null : origUrl.endsWith("/"));
    }

    public static String decodeHost(@NonNull String host){
        return IDN.toUnicode(host);
    }

    public static String encodeHost(@NonNull String host){
        return Arrays.stream(host.split("\\.")).map(IDN::toASCII).collect(Collectors.joining("."));
    }

    public static String decodeFragment(@NonNull String fragment){
        String charset = detectEncodedCharset(fragment);
        return decodeFragment(fragment, charset);
    }
    public static String decodeFragment(@NonNull String fragment, String charset){
        return StringUtils.uriDecode(fragment, Charset.forName(charset));
    }
    
    public static String encodeFragment(@NonNull String fragment){
        String charset = detectEncodedCharset(fragment);
        return encodeFragment(fragment, charset);
    }    
    public static String encodeFragment(@NonNull String fragment, String charset){
        return UriUtils.encodeFragment(fragment, charset);
    }

    public static String getFullFragmentFromUrl(URL u, Boolean slashEnded) {
        String fragment = u.getPath() + (u.getQuery() == null ? "" : "?" + u.getQuery());
        fragment = fragment + (u.getRef() == null ? "" : "#" + u.getRef());

        if (slashEnded != null){
            if (fragment.endsWith("/") && !slashEnded){
                fragment = fragment.substring(0, fragment.length()-1);
            }
            else if (!fragment.endsWith("/") && slashEnded){
                fragment += "/";
            }
        }
        return fragment.trim();
    }

    public static boolean isDecodedHost(String host) {
        return host.equals(IDN.toUnicode(host));
    }

    public static boolean isDecodedFragment(String str) {
        try{
            return str.equals(decodeFragment(str));
        }
        catch (Exception e){
            return false;
        }
    }


    public static String detectEncodedCharset(String str) {
        return detectEncodedCharset(str, UTF8);
    }

    public static String detectEncodedCharset(String str, String defCharset) {
        List<String> charsets = getCharsets();
        for (String charset : charsets){
            try {
                if (str.equals(encodeFragment(decodeFragment(str, charset), charset))) {
                    return charset;
                }
            }
            catch (Exception e){
                //System.out.println("error: charset=" + charset);
                //e.printStackTrace();
            }
        }
        return defCharset;
    }

    public static List<String> getCharsets(){
        String[] charsets = {"UTF-8","UTF-16", "windows-1251","windows-1252","KOI8-U", "US-ASCII","x-IBM300","x-JIS0208","x-MacDingbat",
                "ISO-8859-1","ISO-8859-2","ISO-8859-4","ISO-8859-5","ISO-8859-7","ISO-8859-9","ISO-8859-13","ISO-8859-15","CESU-8",
                "IBM00858","IBM437","IBM775","IBM850","IBM852","IBM855","IBM857","IBM862","IBM866",
                "KOI8-R","UTF-16BE","UTF-16LE","UTF-32","UTF-32BE","UTF-32LE",
                "x-UTF-32BE-BOM","x-UTF-32LE-BOM",
                "windows-1250","windows-1253","windows-1254","windows-1257",
                "x-IBM737","x-IBM874","x-UTF-16LE-BOM",
                "Canonical","Big5","Big5-HKSCS","EUC-JP","EUC-KR","GB18030","GB2312","GBK","IBM-Thai",
                "IBM01140","IBM01141","IBM01142","IBM01143","IBM01144","IBM01145","IBM01146","IBM01147","IBM01148",
                "IBM01149","IBM037","IBM1026","IBM1047","IBM273","IBM277","IBM278","IBM280","IBM284","IBM285",
                "IBM290","IBM297","IBM420","IBM424","IBM500","IBM860","IBM861","IBM863","IBM864","IBM865","IBM868",
                "IBM869","IBM870","IBM871","IBM918",
                "ISO-2022-CN","ISO-2022-JP","ISO-2022-JP-2","ISO-2022-KR","ISO-8859-3","ISO-8859-6","ISO-8859-8",
                "JIS_X0201","JIS_X0212-1990","Shift_JIS","TIS-620",
                "windows-1255","windows-1256","windows-1258","windows-31j",
                "x-Big5-Solaris","x-euc-jp-linux","x-EUC-TW","x-eucJP-Open",
                "x-IBM1006","x-IBM1025","x-IBM1046","x-IBM1097","x-IBM1098","x-IBM1112","x-IBM1122","x-IBM1123","x-IBM1124","x-IBM1166","x-IBM1364","x-IBM1381","x-IBM1383","x-IBM33722","x-IBM833","x-IBM834","x-IBM856","x-IBM875","x-IBM921","x-IBM922","x-IBM930","x-IBM933","x-IBM935","x-IBM937","x-IBM939","x-IBM942","x-IBM942C","x-IBM943","x-IBM943C","x-IBM948","x-IBM949","x-IBM949C","x-IBM950","x-IBM964","x-IBM970",
                "x-ISCII91","x-ISO2022-CN-CNS","x-ISO2022-CN-GB","x-iso-8859-11","x-JISAutoDetect","x-Johab","x-MacArabic","x-MacCentralEurope","x-MacCroatian","x-MacCyrillic","x-MacGreek","x-MacHebrew","x-MacIceland","x-MacRoman","x-MacRomania","x-MacSymbol","x-MacThai","x-MacTurkish","x-MacUkraine","x-MS932_0213","x-MS950-HKSCS","x-MS950-HKSCS-XP","x-mswin-936","x-PCK","x-SJIS_0213","x-windows-50220","x-windows-50221","x-windows-874","x-windows-949","x-windows-950","x-windows-iso2022jp"};

        return Arrays.asList(charsets);
    }


}
