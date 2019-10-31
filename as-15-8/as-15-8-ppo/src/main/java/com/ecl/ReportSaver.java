package com.ecl;

/**
 * Фасад для сохранения отчетов
 *
 * User: asinjavin
 * Date: 11.10.2019
 * Time: 15:05
 */
public interface ReportSaver
{
    void saveReport(String report, byte data[], ReportPeriod reportPeriod, String mime );
}
