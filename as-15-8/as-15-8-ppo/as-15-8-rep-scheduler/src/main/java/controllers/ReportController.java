package controllers;

import controllers.ex.ReportNotCreated;
import controllers.ex.ReportNotFound;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repositories.*;

import java.util.List;
import java.util.Optional;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 19:21
 */
@RestController
@RequestMapping("reports")
public class ReportController
{
    private final ReportAdminStatRepository reportAdminStatRepository;
    private final ReportStatRepository reportStatRepository;
    private final RegReportsTableRepository regReportsTableRepository;
    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final ReportAdminTableRepository reportAdminTableRepository;

    @Autowired
    public ReportController(ReportAdminStatRepository reportAdminStatRepository,
                            ReportStatRepository reportStatRepository,
                            RegReportsTableRepository regReportsTableRepository,
                            ReportRepository reportRepository,
                            ReportTypeRepository reportTypeRepository, ReportAdminTableRepository reportAdminTableRepository) {
        this.reportAdminStatRepository = reportAdminStatRepository;
        this.reportStatRepository = reportStatRepository;
        this.regReportsTableRepository = regReportsTableRepository;
        this.reportRepository = reportRepository;
        this.reportTypeRepository = reportTypeRepository;
        this.reportAdminTableRepository = reportAdminTableRepository;
    }

    /**
     * 1 Запрос на получение статистики по сформированным регламентным отчетам
     * select * from dm.v_api_reg_reports_stat;
     *
     * @return
     */
    @GetMapping("stat")
    public List<ReportStat> getStat() {
        return reportStatRepository.findAll();
    }

    /**
     * 2 Запрос на получение описания отчета по типу отчета
     * select * from dm.rep_tp where rep_tp_id = ?
     *
     * @param rep_tp_id
     * @return
     */
    @GetMapping("{rep_tp_id}")
    public ReportType getReportType(@PathVariable long rep_tp_id) {
        Optional<ReportType> res = reportTypeRepository.findById(rep_tp_id);
        if (!res.isPresent()) throw new ReportNotFound();
        return res.get();
    }

    /**
     * 3 Запрос на получение таблицы по типу отчета
     * select * from dm.v_api_reg_reports_table where rep_tp_id=?
     *
     * @return
     */
    @GetMapping("table/{rep_tp_id}")
    public List<RegReportsTable> getRegReportsTable(@PathVariable long rep_tp_id) {
        return regReportsTableRepository.findAllByRepTpId(rep_tp_id);
    }


    /**
     * 4. Запрос получения файла отчета по rep_id
     *
     * @param rep_id
     * @return
     */
    @GetMapping("data/{rep_id}")
    ResponseEntity<byte[]> getData(@PathVariable long rep_id) {
        Optional<Report> res = reportRepository.findByRepId(rep_id);
        if (!res.isPresent()) throw new ReportNotFound();

        Report report = res.get();
        if (report.getStatus() != ReportStatus.OK) throw new ReportNotCreated();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.parseMediaType(report.getMime()));

        return new ResponseEntity<>(res.get().getData(), responseHeaders, HttpStatus.OK);
    }


    /**
     * 5. Запрос для получения статистики о отчетах для админов
     * select * from dm.v_api_reg_reports_admin_stat;
     *
     * @return
     */
    @GetMapping("admin/stat")
    List<ReportAdminStat> getReportAdminStats() {
        return reportAdminStatRepository.findAll();
    }

    /**
     * 6. Запрос для таблицы для админа по типу отчета
     * select * from  v_api_reg_reports_admin_table rep_tp_id=?
     *
     * @return
     */
    @GetMapping("admin/table/{rep_tp_id}")
    List<ReportAdminTable> getReportAdminTable(@PathVariable long rep_tp_id) {
        return reportAdminTableRepository.findByRepTpId(rep_tp_id);
    }

    /*
7. Запрос по перевыпуску отчета по rep_id и format
     */


}
