package controllers;

import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import model.ParamReport;
import model.ParamReportStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ParamReportRepository;
import repositories.ParamReportStatRepository;

import java.util.List;

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
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    @GetMapping("stat")
    public List<ParamReportStat> getStat() {
        return paramReportStatRepository.findAll();
    }


    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    @GetMapping("table")
    public ResponseEntity<Page<ParamReport>> getSearchSystemPage(@RequestParam(required = false) SortingDirection sortingDirection,
                                                                 @RequestParam(required = false) String sortingColumn,
                                                                 @RequestParam(defaultValue = "0") int pageNumber,
                                                                 @RequestParam(defaultValue = "10") int pageSize) {

            Pageable page = PageRequest.of(pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
            Page<ParamReport> result = paramReportRepository.findAll(page);
            return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
