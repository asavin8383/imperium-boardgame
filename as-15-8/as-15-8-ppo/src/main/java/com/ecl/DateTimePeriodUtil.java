package com.ecl;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Утилита для создания отчетных периодов относительно заданной даты
 *
 * User: asinjavin
 * Date: 10.10.2019
 * Time: 14:37
 */
@Service
public class DateTimePeriodUtil
{
    private ReportPeriod period(String  name, DateTime start, DateTime end) {
        return new ReportPeriod(name, start.toString("yyyy-MM-dd'T'HH:mm:ss"), end.toString("yyyy-MM-dd'T'HH:mm:ss"));
    }

    ReportPeriod getLastDay (Date src) {
        DateTime base = new DateTime(src).withTimeAtStartOfDay();
        DateTime start = base.minusDays(1);
        DateTime end = base.minusSeconds(1);
        return period("день", start, end);
    }

    ReportPeriod getLastWeek(Date src) {
        DateTime base = new DateTime(src).withTimeAtStartOfDay();
        DateTime start = base.withDayOfWeek(1).minusWeeks(1);
        DateTime end = base.withDayOfWeek(1).minusSeconds(1);
        return period("неделя", start, end);
    }

    ReportPeriod getLastMonth(Date src) {
        DateTime base = new DateTime(src).withTimeAtStartOfDay();
        DateTime start = base.withDayOfMonth(1).minusMonths(1);
        DateTime end = base.withDayOfMonth(1).minusSeconds(1);
        return period("месяц", start, end);
    }

    ReportPeriod getLastQuarter(Date src) {
        DateTime base = new DateTime(src).withTimeAtStartOfDay();
        DateTime start = base.withDayOfMonth(1).minusMonths(3);
        DateTime end = base.withDayOfMonth(1).minusSeconds(1);
        return period("квартал", start, end);
    }

    ReportPeriod getLastHalf(Date src) {
        DateTime base = new DateTime(src).withTimeAtStartOfDay();
        DateTime start = base.withDayOfMonth(1).minusMonths(6);
        DateTime end = base.withDayOfMonth(1).minusSeconds(1);
        return period("полугодие", start, end);
    }

    ReportPeriod getLastYear(Date src) {
        DateTime base = new DateTime(src).withTimeAtStartOfDay();
        DateTime start = base.withDayOfYear(1).minusYears(1);
        DateTime end = base.withDayOfYear(1).minusSeconds(1);
        return period("год", start, end);
    }

    boolean isQuarterStart(Date date) {
        DateTime dateTime = new DateTime(date);
        int month = dateTime.getMonthOfYear();
        return month % 3 == 0;
    }

    boolean isHalfStart(Date date) {
        DateTime dateTime = new DateTime(date);
        int month = dateTime.getMonthOfYear();
        return month % 6 == 0;
    }
}
