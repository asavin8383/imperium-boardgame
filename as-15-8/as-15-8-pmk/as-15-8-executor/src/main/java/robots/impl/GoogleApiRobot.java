package robots.impl;

import checkUnits.CheckUnit;
import enums.AccessToolParameter;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import robots.Robot;
import robots.exceptions.ExecutionException;
import robots.utils.EqualityTest;
import robots.utils.ScreenshotFromTextMaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Slf4j
public class GoogleApiRobot implements Robot{

	/** Сервис доступа к REST api */
	private static RestTemplate restTemplate = new RestTemplate();
	
	private static final String GOOGLE_API_URL = "https://www.googleapis.com/customsearch/v1";
	
	private static final String SEARCH_FIELDS = "queries(request, nextPage/startIndex), items(snippet, link)";
	
	private String searchSystemID;
	
	private String key;
	
	private String region;
	
	private int searchLimit;
	
	public GoogleApiRobot(Map<AccessToolParameter, String> scriptParams) {
		this.searchSystemID = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_ID);
		this.key = scriptParams.get(AccessToolParameter.SEARCH_SYSTEM_KEY);
		this.region = scriptParams.get(AccessToolParameter.SEARCH_REGION);
		this.searchLimit = Integer.parseInt(scriptParams.get(AccessToolParameter.SEARCH_RESULT_LIMIT));
	}

	@Override
	public ExecutionJobResult run(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException {
		try {
			EqualityTest test = EqualityTest.forCheckUnit(checkUnit);
			Map<String, List<String>> urls = searchUrlsInGoogle(checkUnit.getValue());
			
			for(Entry<String, List<String>> entry : urls.entrySet()) {
				for(String url : entry.getValue()) {
					if(test.equalTo(url))
						return createExecutionResult(true, entry.getKey());
				}
			}
			return createExecutionResult(false, urls.keySet().iterator().next());
		} catch(Exception ex) {
			throw new ExecutionException(ex);
		}
	}

	@Override
	public void destroy() throws IOException { }
	
	private Map<String, List<String>> searchUrlsInGoogle(String query) throws JSONException, ExecutionException {
		String url = createSearchUrl(query);

		log.info("Поиск в Google по запросу: "+query);
		
		Map<String, List<String>> resp = new HashMap<>();
		int startIndex = 1;
		while(startIndex < this.searchLimit) {			
			String curURL = url.toString()+"&start="+startIndex;
			ResponseEntity<String> response = restTemplate.getForEntity(curURL, String.class);
			if(response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
				JSONObject searchResult = new JSONObject(response.getBody());
				List<String> urls = new ArrayList<>();
				try {
					JSONArray urlsJson = searchResult.getJSONArray("items");
					int limit = Math.min(urlsJson.length(), this.searchLimit - startIndex + 1);
					for(int i = 0; i < limit; i++) {
						urls.add(urlsJson.getJSONObject(i).getString("link"));
					}
					
				} catch(org.json.JSONException ex) {}
				
				resp.put(formatResponse(response), urls);
				try {
					startIndex = searchResult.getJSONObject("queries").getJSONArray("nextPage").getJSONObject(0).getInt("startIndex");
				} catch (Exception ex) {
					break;
				}
			} else {
				throw new ExecutionException("Ошибка! От Google получен ответ, отличный от ожижаемого: status: "+response.getStatusCodeValue()+", body: "+response.getBody());
			}
		}
		return resp;
	}
	
    private ExecutionPSJobResult createExecutionResult(boolean linkFound, String responseContent) {
        ExecutionPSJobResult message = new ExecutionPSJobResult();
        message.setLinkFound(linkFound);
        message.setScreenshot(ScreenshotFromTextMaker.makeScreenshot(responseContent));
        return message;
    }
    
    private String formatResponse(ResponseEntity<String> response) {
    	StringBuilder builder = new StringBuilder();
    	builder.append("Status: ");
    	builder.append(response.getStatusCode().toString());
		builder.append("\n\n");
    	builder.append("Body:\n");
    	builder.append(response.getBody());
    	builder.append("\n");
    	builder.append("Headers:\n");
    	for(Entry<String, List<String>> header : response.getHeaders().entrySet()) {
    		builder.append("    "+header.getKey() + ": ");
    		builder.append(header.getValue().stream().map(str -> "\""+str+"\"").collect(Collectors.joining("; ")));
    		builder.append("\n");
    	}
    	return builder.toString();
    }
    
    private String createSearchUrl(String searchQuery) {
    	StringBuilder builder = new StringBuilder(GOOGLE_API_URL+"?")
    			.append("cx="+this.searchSystemID+"&")
    			.append("key="+this.key+"&")
    			.append("fields="+SEARCH_FIELDS+"&")
    			.append("q="+searchQuery);
    	if(Strings.isNotEmpty(this.region))
    		builder.append("cr="+this.region);
    	return builder.toString();
    }
    
	/*public static void main(String[] args) throws ExecutionException, IOException {
		GoogleApiRobot script = new GoogleApiRobot("008760942635674233646:iomr_wlnhoi", "AIzaSyDD-B6NrOjC_Vyjhezdo8EruwH-xrqdT-8", "countryRU", 15);
		ExecutionPSJobResult result = (ExecutionPSJobResult)script.run(new CheckUnit(CheckUnitType.URL, "https://cannabay.org"));
		System.out.println(result.isLinkFound());
		
		ByteArrayInputStream bis = new ByteArrayInputStream(result.getScreenshot());
		BufferedImage bImage2 = ImageIO.read(bis);
		ImageIO.write(bImage2, "png", new File("D:\\output.png") );
		
		script.destroy();
	}*/
}
