package common;

import exceptions.ExceptionErdiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import repositories.*;
import restapi.ErdiRestClient;

import javax.annotation.PostConstruct;
import java.io.IOException;


/**
 * Конфигурация АС 15.8
 *
 */
@SpringBootApplication
@ComponentScan(basePackages= {"common", "repositories", "controllers", "services", "restapi"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
@EnableScheduling
@EnableAsync
public class ApplicationConfiguration {

	@Autowired
	ContentVersionRepository contentVersionRepository;

	@Autowired
	AddonVersionRepository addonVersionRepository;

	@Autowired
	ContentHistoryRepository contentHistoryRepository;

	@Autowired
	ContentInfoRepository contentInfoRepository;

	@Autowired
	RestApiHelper restApiHelper;

    @Autowired
    ErdiRestClient erdiRestClient;

    @Autowired
    ContentRepository contentRepository;

    @Bean
    public RestTemplate restTemplateInit() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor("test158", "test158"));
        return restTemplate;
    }

    // todo async security
	/*@PostConstruct
	public void init() {
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}*/

    public static void main(String[] args) {
		SpringApplication.run(ApplicationConfiguration.class, args);
	}

	@PostConstruct
	public void test() throws IOException, ExceptionErdiParser {
		System.out.println("START ---------------------");


		//restApiHelper.test1();
		//restApiHelper.test2();
		//restApiHelper.test3();


		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//restApiHelper.test4(dateFormat.parse("2017-09-22 09:23:13"));

		//restApiHelper.test5(140);
		//restApiHelper.test6();
		//restApiHelper.test7();

		/*
		List<ContentVersion> list = contentVersionRepository.findAll();
		System.out.println("size = " + list.size());
		System.out.println(list.toString());
		*/

		/*
		List<AddonVersion> addons = addonVersionRepository.findAll();
		System.out.println("size = " + addons.size());
		System.out.println(addons.toString());
		*/


		//restApiHelper.test88();

		/*
		List<ContentInfo> list = contentInfoRepository.findAllById(Arrays.asList(1L, 2L));
		System.out.println("ContentInfo --->");
		System.out.println( list.toString() );
		*/

		/*
		Optional<ContentInfo> opt = contentInfoRepository.findById(2L);
		System.out.println("ContentInfo --->");
		System.out.println( opt.toString() );
		*/

		/*
		RestTemplate restTemplate = new RestTemplate();
		Quote quote = restTemplate.getForObject("https://gturnquist-quoters.cfapps.io/api/random", Quote.class);
		log.info(quote.toString());
		*/

		/*
        System.out.println("test1 --->" + parameterService.getParamValue("TEST1"));
        System.out.println("test2 --->" + parameterService.getParamValue("test2"));
        System.out.println("test2 --->" + parameterService.getParamValue("test2", null));
        System.out.println("test2 --->" + parameterService.getParamValue("test2", false));
        System.out.println("test3 --->" + parameterService.getParamValue("test2", true));
        */


		//erdiRestClient.fillFullErdiToDB();
		//erdiRestClient.removeLastContentVersion();
		//erdiRestClient.removeLastContentVersion();
		//erdiRestClient.removeLastContentVersion();
		//erdiRestClient.removeLastContentVersion();

		System.out.println("END ---------------------");

	}
}
