package com.ecl;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: asinjavin
 * Date: 10.10.2019
 * Time: 17:51
 */
public class DateTest
{

    private Date date(String s) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(s);
    }

    @Test
    public void testDay() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();
        Date date = date("2019-01-05T00:05:00");

        ReportPeriod reportPeriod = dateTimePeriodUtil.getLastDay(date);
        Assert.assertEquals("2019-01-04T00:00:00", reportPeriod.getFrom());
        Assert.assertEquals("2019-01-04T23:59:59", reportPeriod.getTo());
    }

    @Test
    public void testWeek() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();
        Date date = date("2019-01-05T00:05:00");

        ReportPeriod reportPeriod = dateTimePeriodUtil.getLastWeek(date);
        Assert.assertEquals("2018-12-24T00:00:00", reportPeriod.getFrom());
        Assert.assertEquals("2018-12-30T23:59:59", reportPeriod.getTo());
    }

    @Test
    public void testMonth() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();
        Date date = date("2019-01-05T00:05:00");

        ReportPeriod reportPeriod = dateTimePeriodUtil.getLastMonth(date);
        Assert.assertEquals("2018-12-01T00:00:00", reportPeriod.getFrom());
        Assert.assertEquals("2018-12-31T23:59:59", reportPeriod.getTo());
    }

    @Test
    public void testQuarter() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();

        Date date = date("2019-01-05T00:05:00");
        ReportPeriod reportPeriod = dateTimePeriodUtil.getLastQuarter(date);

        Assert.assertEquals("2018-10-01T00:00:00", reportPeriod.getFrom());
        Assert.assertEquals("2018-12-31T23:59:59", reportPeriod.getTo());
    }

    @Test
    public void testHalf() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();

        Date date = date("2019-01-05T00:05:00");
        ReportPeriod reportPeriod = dateTimePeriodUtil.getLastHalf(date);

        Assert.assertEquals("2018-07-01T00:00:00", reportPeriod.getFrom());
        Assert.assertEquals("2018-12-31T23:59:59", reportPeriod.getTo());
    }

    @Test
    public void testYear() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();

        Date date = date("2019-01-05T00:05:00");
        ReportPeriod reportPeriod = dateTimePeriodUtil.getLastYear(date);

        Assert.assertEquals("2018-01-01T00:00:00", reportPeriod.getFrom());
        Assert.assertEquals("2018-12-31T23:59:59", reportPeriod.getTo());
    }

    @Test
    public void testStart() throws ParseException {
        DateTimePeriodUtil dateTimePeriodUtil = new DateTimePeriodUtil();

        Date date = date("2019-01-05T00:05:00");
        Assert.assertFalse(dateTimePeriodUtil.isHalfStart(date));

        date = date("2019-03-01T00:05:00");
        Assert.assertTrue(dateTimePeriodUtil.isQuarterStart(date));
        Assert.assertFalse(dateTimePeriodUtil.isHalfStart(date));

        date = date("2019-06-01T00:05:00");
        Assert.assertTrue(dateTimePeriodUtil.isQuarterStart(date));
        Assert.assertTrue(dateTimePeriodUtil.isHalfStart(date));

    }
}
