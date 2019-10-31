package com.ecl;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Стартовый класс
 *
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 16:02
 */
@Log
@SpringBootApplication
@EnableScheduling
public class Application
{
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}