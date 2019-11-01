package schedule;

import model.Report;

/**
 * Фасад для создания отчета
 *
 * User: asinjavin
 * Date: 11.10.2019
 * Time: 15:40
 */
public interface ReportService
{
    void runReport(Report report);
}
