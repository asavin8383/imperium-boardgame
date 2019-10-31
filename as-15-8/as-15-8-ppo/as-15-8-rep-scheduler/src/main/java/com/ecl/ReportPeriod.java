package com.ecl;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Отчетный период
 *
 * User: asinjavin
 * Date: 10.10.2019
 * Time: 14:38
 */
@Data
@AllArgsConstructor
public class ReportPeriod
{
    String name;
    String from;
    String to;

    @Override
    public String toString() {
        return "с " + from + " по " + to;
    }
}
