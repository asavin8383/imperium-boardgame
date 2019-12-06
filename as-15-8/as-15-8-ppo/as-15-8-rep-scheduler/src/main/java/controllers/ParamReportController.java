package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.ex.ReportNotCreated;
import controllers.ex.ReportNotFound;
import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ParamReportRepository;
import repositories.ParamReportStatRepository;

import java.util.List;
import java.util.Optional;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 19:21
 */
@RestController
@RequestMapping("param-reports")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParamReportController
{
    private final ParamReportRepository paramReportRepository;
    private final ParamReportStatRepository paramReportStatRepository;


    /**
     * 1 Запрос на получение статистики по сохраненный параметризированным отчетам
     * select * from dm.v_api_reg_reports_stat;
     *
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_FORMATION_PARAM_REPORT')")
    @GetMapping("stat")
    public List<ParamReportStat> getStat() {
        return paramReportStatRepository.findAll();
    }


    @PreAuthorize("hasAnyRole('ROLE_FORMATION_PARAM_REPORT')")
    @GetMapping("table/{rep_tp_id}")
//    @JsonView(Views.Data.class)
    public Page<ParamReport> getSearchSystemPage(@PathVariable int rep_tp_id,
                                                 @RequestParam(required = false) SortingDirection sortingDirection,
                                                 @RequestParam(required = false) String sortingColumn,
                                                 @RequestParam(defaultValue = "0") int pageNumber,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(defaultValue = "") String query) {

        System.out.println("ParamReportController.getSearchSystemPage");
        System.out.println("query = " + query);
            Pageable page = PageRequest.of(pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
            return paramReportRepository.findByRepTpIdAndQuery(rep_tp_id, query, page);

    }

    @PreAuthorize("hasAnyRole('ROLE_FORMATION_PARAM_REPORT')")
    @GetMapping("data/{rep_id}")
//    @JsonView(Views.Data.class)
    ResponseEntity<byte[]> getData(@PathVariable long rep_id) {
        Optional<ParamReport> res = paramReportRepository.findByRepId(rep_id);
        if (!res.isPresent()) throw new ReportNotFound();

        ParamReport report = res.get();
        HttpHeaders responseHeaders = new HttpHeaders();
        String mime = report.getMime();
        if (mime == null) mime = "application/octet-stream";
        responseHeaders.setContentType(MediaType.parseMediaType(mime));

        return new ResponseEntity<>(res.get().getData(), responseHeaders, HttpStatus.OK);
    }


}
