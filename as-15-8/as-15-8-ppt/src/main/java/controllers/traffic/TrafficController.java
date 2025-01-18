package controllers.traffic;

import enums.SortingDirection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.AccessToolType;
import model.traffic.Traffic;
import model.traffic.TrafficBriefView;
import model.traffic.TrafficFullView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import services.traffic.TrafficService;

@RestController
@RequestMapping(path = "/traffic",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_PREPARATION_TRAFFIC')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class TrafficController {

    private final TrafficService trafficService;

    @Operation(summary = "Получить список всех трафиков", tags = {"Трафик"})
    @GetMapping
    public Page<TrafficBriefView> getBriefTrafficList(
            @Parameter(name = "sortingDirection", in = ParameterIn.QUERY, description = "Направление сортировки") @RequestParam(required = false) SortingDirection sortingDirection,
            @Parameter(name = "sortingColumn", in = ParameterIn.QUERY, description = "Столбец для сортировки") @RequestParam(required = false) String sortingColumn,
            @Parameter(name = "pageNumber", in = ParameterIn.QUERY, description = "Номер страницы") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(name = "pageSize", in = ParameterIn.QUERY, description = "Количество элементов на странице") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(name = "query", in = ParameterIn.QUERY, description = "Поисковый запрос") @RequestParam(required = false) String query,
            @Parameter(name = "accessToolType", in = ParameterIn.QUERY, description = "Тип инструмента доступа") @RequestParam(required = false) AccessToolType accessToolType,
            @Parameter(name = "name", in = ParameterIn.QUERY, description = "Имя трафика") @RequestParam(required = false) String name) {

        return trafficService.getBriefTrafficList(sortingDirection, sortingColumn,
                pageNumber, pageSize, query, accessToolType, name);
    }

    @Operation(summary = "Актуализировать количество чек юнитов для трафика", tags = {"Трафик"})
    @PutMapping(path = "/actual_check_units")
    public ResponseEntity calculateActualCheckUnits(
            @Parameter(name = "id", in = ParameterIn.QUERY, description = "id трафика", schema = @Schema(type = "integer")) @RequestParam("id") Traffic traffic) {
        try {
            trafficService.actualizeTraffic(traffic.getId());
            return ResponseEntity.ok().body(traffic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при подсчёте актуального числа чек юнитов для трафика");
        }

    }

    @PutMapping(path = "/actual_check_units_all")
    @Operation(summary = "Актуализировать количество чек юнитов для всех трафиков", tags = {"Трафик"})
    public ResponseEntity calculateActualCheckUnitsForAllTraffic() {
        try {
            trafficService.actualizeCheckUnitsCountForAllTraffic();
            return ResponseEntity.ok().body("Чек юниты для всех трафиков актуализаированы");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при подсчёте актуального числа чек юнитов для всех трафиков");
        }
    }

    @Transactional
    @Operation(summary = "Получить трафик по id", tags = {"Трафик"})
    @GetMapping(path = "/{id}")
    public TrafficFullView getTrafficById(
            @Parameter(name = "id", in = ParameterIn.PATH, description = "id трафика") @PathVariable Long id) {
        return trafficService.getTrafficById(id);
    }

    @Transactional
    @Operation(summary = "Создать трафик", tags = {"Трафик"})
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public TrafficFullView createTraffic() {
        TrafficFullView trafficFullView = trafficService.createTraffic();
        trafficService.actualizeTraffic(trafficFullView.getId());
        return trafficFullView;
    }

    @Transactional
    @Operation(summary = "Изменить трафик", tags = {"Трафик"})
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TrafficFullView updateTraffic(
            @Parameter(name = "trafficFullView", description = "Обновленный трафик", schema = @Schema(implementation = TrafficFullView.class)) @RequestBody TrafficFullView fullView,
            @Parameter(name = "id", in = ParameterIn.PATH, description = "id трафика", schema = @Schema(type = "integer")) @PathVariable("id") Traffic traffic) {
        TrafficFullView trafficFullView = trafficService.updateTraffic(fullView, traffic);
        trafficService.actualizeTraffic(traffic.getId());
        return trafficFullView;
    }

    @Operation(summary = "Удалить трафик", tags = {"Трафик"})
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteTraffic(@Parameter(name = "id", in = ParameterIn.PATH, description = "id трафика") @PathVariable Long id) {
        trafficService.deleteTraffic(id);
    }

    @Operation(summary = "Импортировать ресурсы трафика из файла", tags = {"Трафик"})
    @PutMapping(path = "{id}/import-resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity updateTrafficFromFile(@Parameter(name = "id", in = ParameterIn.PATH, description = "id трафика") @PathVariable("id") Long trafficId,
                                                @Parameter(name = "file", description = "Файл ресурсов трафика") @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(trafficService.updateTrafficFromFile(trafficId, file));
    }

}
