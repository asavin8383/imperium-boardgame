package com.ecl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность для чтения информации из файла конфигурации Spring
 * application.yml или properties.
 * Нужен для чтения набора имен отчетов
 *
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 17:41
 */
@Component
@ConfigurationProperties("app.reports")
@Data
class ReportsBy
{
    List<String> day = new ArrayList<>();
    List<String> week = new ArrayList<>();
    List<String> month = new ArrayList<>();
    List<String> quarter = new ArrayList<>();
    List<String> half = new ArrayList<>();
    List<String> year = new ArrayList<>();
}