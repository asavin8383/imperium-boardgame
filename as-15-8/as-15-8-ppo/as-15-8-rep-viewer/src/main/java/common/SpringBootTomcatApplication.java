package common;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * User: asinjavin
 * Date: 21.11.2019
 * Time: 22:47
 */
@SpringBootApplication
//        (exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class SpringBootTomcatApplication extends SpringBootServletInitializer
{
}