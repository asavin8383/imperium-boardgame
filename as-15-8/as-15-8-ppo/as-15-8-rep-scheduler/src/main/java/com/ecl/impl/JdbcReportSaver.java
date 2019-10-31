package com.ecl.impl;

import com.ecl.ReportPeriod;
import com.ecl.ReportSaver;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * Реализация сохранения отчетов в базу через JDBC
 *
 * User: asinjavin
 * Date: 11.10.2019
 * Time: 15:05
 */
@Service
public class JdbcReportSaver implements ReportSaver
{
    private final DataSource dataSource;

    @Autowired
    public JdbcReportSaver(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Synchronized
    public void saveReport(String report, byte data[], ReportPeriod reportPeriod, String mime) {
        System.out.println("save " + data.length + " " + reportPeriod);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("insert into trash.reports(rep_name, data, start, finish, type, mime) values (?, ?, ?, ?, ?, ?)",
                report,
                data,
                LocalDateTime.parse(reportPeriod.getFrom()),
                LocalDateTime.parse(reportPeriod.getTo()),
                reportPeriod.getName(),
                mime
        );
    }
}
