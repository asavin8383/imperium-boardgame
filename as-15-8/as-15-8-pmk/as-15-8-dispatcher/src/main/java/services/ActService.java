package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repositories.ResultRepo;
import rest.ActRequest;
import webClients.PodWebClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final PodWebClient webClient;
    private final ResultRepo arrangementResultRepo;


    public boolean createAct(Long arragementId){
        log.info("Подготовка данных для акта. ID мероприятия: {}", arragementId);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            Date minDate = ldt2date(arrangementResultRepo.getMinDateByArrangementId(arragementId));
            Date maxDate = ldt2date(arrangementResultRepo.getMaxDateByArrangementId(arragementId));

            ActRequest actRequest = new ActRequest();
            actRequest.setArragementId(arragementId);
            actRequest.setStartDate(minDate != null ? dateFormat.format(minDate) : "");
            actRequest.setEndDate(maxDate != null ? dateFormat.format(maxDate) : "");

            return webClient.sendAct(actRequest);
        } catch (Exception ex){
            log.error("Ошибка при формировании данных для акта по мероприятию: "+arragementId);
            return false;
        }
    }

    private Date ldt2date(LocalDateTime ldt){
        if (ldt == null)
            return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

}
