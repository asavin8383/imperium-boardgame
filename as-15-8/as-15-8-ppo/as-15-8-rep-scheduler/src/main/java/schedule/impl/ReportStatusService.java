package schedule.impl;

import lombok.extern.slf4j.Slf4j;
import model.Report;
import model.ReportStatus;
import org.springframework.stereotype.Service;
import repositories.ReportRepository;

import javax.transaction.Transactional;
import java.sql.Timestamp;

/**
 * Реализация для создания отчета по http
 *
 * User: asinjavin
 * Date: 08.10.2019
 * Time: 19:49
 */
@Slf4j
@Service
public class ReportStatusService
{
    private final ReportRepository reportRepository;

    public ReportStatusService(ReportRepository reportRepository) {this.reportRepository = reportRepository;}

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void markStarted(Report report) {
        report.setStart_dttm(new Timestamp(System.currentTimeMillis()));
        report.setFinish_dttm(null);
        report.setStatus(ReportStatus.RUNNING);
        report.setReason(null);
        report.setMime(null);
        reportRepository.saveAndFlush(report);

    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void markDone(Report report, String mime, byte[] data) {
        report.setMime(mime);
        report.setFinish_dttm(new Timestamp(System.currentTimeMillis()));
        report.setStatus(ReportStatus.OK);
        report.setData(data);
        reportRepository.saveAndFlush(report);

    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void markFailed(Report report, Throwable e) {
        report.setStatus(ReportStatus.FAILED);
        report.setReason(e.getMessage());
        reportRepository.saveAndFlush(report);
    }
}
