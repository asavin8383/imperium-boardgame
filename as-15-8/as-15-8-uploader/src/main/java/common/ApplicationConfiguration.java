package common;

import exceptions.ExceptionErdiParser;
import model.scheme.ContentHistory;
import model.scheme.ContentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import repositories.AddonVersionRepository;
import repositories.ContentHistoryRepository;
import repositories.ContentInfoRepository;
import repositories.ContentVersionRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Конфигурация АС 15.8
 *
 */
@SpringBootApplication
@ComponentScan(basePackages= {"common", "repositories", "schedule"})
@EnableJpaRepositories("repositories")
@EntityScan("model")
@EnableScheduling
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


		System.out.println("END ---------------------");

	}
}
