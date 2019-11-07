package services;

import enums.CheckUnitJobResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.ResultRepo;
import rest.ActRequest;
import rest.ActCheckResult;
import rest.ResponseStatusString;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActService {

    @Value("${gateway.url}")
    private String configUrl;

    private final OAuth2RestTemplate oauth2RestTemplate;
    private final ResultRepo arrangementResultRepo;
    private static final int defMaxCountScreenShots = 10;


    public boolean createAct(Long arragementId){
        List<Result> list = arrangementResultRepo.findByArrangementId(arragementId);
        log.info("Подготовка данных для акта. ID мероприятия: {}. Всего результатов: {}",
                arragementId, list.size());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        List<ActCheckResult> checkResults = new ArrayList<>();
        List<String> topScreenShots = new ArrayList<>();

        int maxCountScreenShots = defMaxCountScreenShots;
        Set<CheckUnitJobResult> allowResults =
                new HashSet<>(Arrays.asList(CheckUnitJobResult.values()));

        for (Result ares : list){
            ActCheckResult checkResult = new ActCheckResult();
            checkResult.checkResultId = ares.getId();
            checkResult.checkUnitType = ares.getCheckUnitType();
            checkResult.checkUnitValue = ares.getCheckUnitValue();

            Date endDate = ldt2date(ares.getEndDate());
            checkResult.date = endDate == null ? "" : dateFormat.format(endDate);
            checkResults.add(checkResult);

            if (maxCountScreenShots < 0 || topScreenShots.size() < maxCountScreenShots){
                byte[] screen = ares.getScreenshot();
                if (screen != null && screen.length > 0){
                    if (allowResults.contains(ares.getResult())){
                        String screenBase64 = Base64.getEncoder().encodeToString(screen);
                        topScreenShots.add(screenBase64);
                    }
                }
            }
        }

        Date minDate = ldt2date(arrangementResultRepo.getMinDateByArrangementId(arragementId));
        Date maxDate = ldt2date(arrangementResultRepo.getMaxDateByArrangementId(arragementId));

        ActRequest actRequest = new ActRequest();
        actRequest.arragementId = arragementId;
        actRequest.checkResults = checkResults;
        actRequest.startDate = minDate != null ? dateFormat.format(minDate) : "";
        actRequest.endDate = maxDate != null ? dateFormat.format(maxDate) : "";
        actRequest.topScreenShots = topScreenShots;

        ResponseStatusString res = send(actRequest);

        return res.status;
    }

    public ResponseStatusString send(@NonNull ActRequest actRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ActRequest> entity = new HttpEntity<>(actRequest, headers);

        log.info("Отправка данных на ПОД для формирования акта: {}. Кол-во результатов: {}. Кол-во скриншотов: {}",
                actRequest,
                actRequest.checkResults.size(),
                actRequest.topScreenShots.size());

        ResponseStatusString response = oauth2RestTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(configUrl).path("/pod/create_act").build().toString(),
                entity,
                ResponseStatusString.class);

        log.info("Статус отправки акта в ПОД: {}. Акт: {}", response, actRequest);

        return response;
    }

    private Date ldt2date(LocalDateTime ldt){
        if (ldt == null)
            return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

}
