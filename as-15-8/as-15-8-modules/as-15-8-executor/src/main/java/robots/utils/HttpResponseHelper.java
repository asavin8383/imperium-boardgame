package robots.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.springframework.util.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

        for (Iterator<LogEntry> it = logs.iterator(); it.hasNext();) {
            LogEntry entry = it.next();

            try {
                JSONObject json = new JSONObject(entry.getMessage());
                JSONObject message = json.getJSONObject("message");
                String method = message.getString("method");

                if (method != null && "Network.responseReceived".equals(method)) {
                    JSONObject params = message.getJSONObject("params");
                    JSONObject response = params.getJSONObject("response");
                    String messageUrl = response.getString("url");

                    if (currentURL.equals(messageUrl)) {
                        int status = response.getInt("status");
                        rows.add(new HttpResponseMeta(messageUrl, response.getJSONObject("headers"), status, true));
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
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
