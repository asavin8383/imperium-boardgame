package utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DomainUtil {

	private static final String GOOGLE_SEARCH_DOMAIN = "https://www.google.com";
	
	private static final String SEARCHED_CONTAINERS_CSS = "div#search div.g div.rc > div.r > a:first-child";
	
	private static final String NEXT_PAGE_URL_CSS = "div#foot div#navcnt > table td.navend > a.pn";
	
	public static Set<String> searchDomainsByMask(String domainMask) throws IOException, InterruptedException{
		
		Set<String> domains = new HashSet<>();
		
		String googleSearchUrl = createGoogleSearchQuery(domainMask);
		Set<String> urls = searchUrlsInGoogle(googleSearchUrl);
		
		for(String url : urls) {
			try {
				domains.add(new URL(url).getHost());
			} catch (MalformedURLException ex) {
				log.warn("Не удалось получить домен из url: "+url);
			}
		}
		return domains;
	}
	
	private static Set<String> searchUrlsInGoogle(String searchUrl) throws IOException, InterruptedException {
		
		Set<String> urls = new HashSet<>();
		String nextUrl = searchUrl;
		while(nextUrl != null) {			
			Document page = loadPage(nextUrl);
			Elements searchedContainers = page.select(SEARCHED_CONTAINERS_CSS);
			for(Element searchedContainer : searchedContainers) {
				if(searchedContainer.hasAttr("href")) {
					String url = searchedContainer.attr("href");
					if(Strings.isNotEmpty(url))
						urls.add(url);
				}
			}
			Element nextPageButton = page.select(NEXT_PAGE_URL_CSS).last();
			if(nextPageButton != null && nextPageButton.hasAttr("href")) {
				nextUrl = GOOGLE_SEARCH_DOMAIN + nextPageButton.attr("href");
				Thread.sleep(5000);
			} else {
				nextUrl = null;
			}
		}
		return urls;
	}
	
	private static String createGoogleSearchQuery(String domainMask) {
		String domain = domainMask.replaceAll("\\*\\.", "");
		StringBuilder builder = new StringBuilder(GOOGLE_SEARCH_DOMAIN);
		builder.append("/search?q=");
		builder.append("site:").append(domain).append("+");
		builder.append("-inurl:https://").append(domain).append("+");
		builder.append("-inurl:https://www.").append(domain).append("+");
		builder.append("-inurl:http://").append(domain).append("+");
		builder.append("-inurl:http://www.").append(domain);
		return builder.toString();
	}
	
	private static Document loadPage(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36")
                .timeout(60000)
                .get();    
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		searchDomainsByMask("*.rutracker.org")
			.forEach(domain -> System.out.println(domain));
	}
}
