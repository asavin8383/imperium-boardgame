package robots.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
public class HttpResponseHelper {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HttpResponseMeta {
        public String url;
        public JSONObject jsonHeaders;
        public Integer status;
        public boolean hasLog;
    }

    public static HttpResponseMeta getGetResponseMeta(WebDriver driver){
        return getGetResponseMeta(driver, false, null);
    }

    public static HttpResponseMeta getGetResponseMeta(WebDriver driver, boolean enableDump, String text){
        List<String> methods = Arrays.asList(
                "Network.requestWillBeSent",
                "Network.responseReceived",
                "Network.loadingFailed",
                "Network.loadingFinished"
        );

        String currentURL = driver.getCurrentUrl();
        LogEntries logs = null;

        try {
            logs = driver.manage().logs().get(LogType.PERFORMANCE);
        }
        catch (InvalidArgumentException e){
            e.printStackTrace();
            return null;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        List<HttpResponseMeta> rows = new ArrayList<>();
        List<JSONObject> logList = new ArrayList<>();

        for (Iterator<LogEntry> it = logs.iterator(); it.hasNext();) {
            LogEntry entry = it.next();

            try {
                JSONObject json = new JSONObject(entry.getMessage());
                JSONObject message = json.getJSONObject("message");
                String method = message.getString("method");

                if ("Network.responseReceived".equals(method)) {
                    JSONObject params = message.getJSONObject("params");
                    JSONObject response = params.getJSONObject("response");
                    String messageUrl = response.getString("url");

                    if (currentURL.equals(messageUrl)) {
                        int status = response.getInt("status");
                        rows.add(new HttpResponseMeta(messageUrl, response.getJSONObject("headers"), status, true));
                    }
                }

                // log
                if (enableDump && methods.contains(method)){
                    String url = null;
                    String type = null;
                    if ("Network.requestWillBeSent".equals(method)){
                        try{
                            JSONObject params = message.getJSONObject("params");
                            JSONObject request = params.getJSONObject("request");
                            url = request.getString("url");
                            type = params.getString("type");
                        }
                        catch(JSONException e){}
                    }
                    else if ("Network.responseReceived".equals(method)){
                        try{
                            JSONObject params = message.getJSONObject("params");
                            JSONObject response = params.getJSONObject("response");
                            url = response.getString("url");
                            type = params.getString("type");
                        }
                        catch(JSONException e){}
                    }

                    JSONObject jsonUrl = null;
                    if (url != null) {
                        if (url.startsWith("data:")){
                            url = url.length() > 25 ? url.substring(0, 20) + "..." : url;
                        }
                        jsonUrl = new JSONObject();
                        jsonUrl.put("url", url);
                        jsonUrl.put("type", type);
                        message.put("service", jsonUrl);
                    }
                    logList.add(message);
                }

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (enableDump){
            log.info("--------------- start log dump ---");
            if (!StringUtils.isEmpty(text))
                log.info(text);
            for(JSONObject o : logList){
                String service = "";
                if (o.has("service")){
                    String type = null;
                    String url = null;
                    try {
                        type = o.getJSONObject("service").getString("type");
                        url = o.getJSONObject("service").getString("url");
                    }
                    catch (JSONException e) {}

                    service += "[type = " + type + ", url = " + url + "] => ";
                    o.remove("service");
                }
                log.info(service + o.toString());
            }
            log.info("--------------- end log dump ---");
        }

        return rows.size() > 0 ? rows.get(rows.size()-1) : null;
    }

    public static Map<String, String> json2map(JSONObject json){
        Map<String, String> res = new HashMap<>();
        if (json == null)
            return res;

        try {
            for (Iterator<?> it = json.keys(); it.hasNext();){
                String key = it.next().toString();
                res.put(key, String.valueOf(json.get(key)));
            }
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
        return res;
    }

    public static String headers2Str(JSONObject o) {
        if (o == null)
            return null;

        try {
            List<String> list = new ArrayList<>();
            for (Iterator<?> it = o.keys(); it.hasNext();) {
                String key = String.valueOf(it.next());
                String value = o.get(key).toString();
                list.add(key + ":" + value);
            }
            Collections.sort(list);

            return String.join( "\r\n", list);
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject str2headers(String str) {
        if (str == null)
            return null;

        try {
            JSONObject o = new JSONObject();
            String[] keyValues = str.split("\r\n");
            for (String kv : keyValues){
                if (!StringUtils.isEmpty(kv)){
                    String[] keyVal = kv.split(":", 2);
                    if (keyVal.length == 2){
                        o.put(keyVal[0], keyVal[1]);
                    }
                }
            }
            return o;
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

}
