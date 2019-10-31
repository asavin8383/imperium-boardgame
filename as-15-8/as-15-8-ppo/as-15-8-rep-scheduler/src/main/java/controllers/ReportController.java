package controllers;

import model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import repositories.ReportRepository;

import java.util.List;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 19:21
 */
@RestController("reports")
public class ReportController
{
    private final ReportRepository reportRepository;

    @Autowired
    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @GetMapping()
    public List<Report> reportList() {
        return reportRepository.findAll();
    }
}
