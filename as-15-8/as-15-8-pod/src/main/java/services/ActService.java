package services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.AS_15_8_POD_Exception;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.rest.control.AccessToolRobot;
import model.rest.control.AccessToolRobotType;
import model.rest.control.ActCheckResultPPP;
import model.rest.control.ActRequestPPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.MissionRepository;
import rest.ActCheckResult;
import rest.ActRequest;
import rest.ArrangementActData;
import rest.ResponseStatusString;
import webClient.DispatcherWebClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActService {

    private final RestTemplate anonymizerRestTemplate;
    private final OAuth2RestTemplate restTemplate;
    private final MissionRepository missionRepository;
    private final DispatcherWebClient dispatcherWebClient;

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    @Value("${gateway.url}")
    private String gatewayUrl;


    @SneakyThrows
    public ResponseStatusString createAct(ActRequest actRequest) {
        log.info("Запрос на отправку акта в ППП: {}", actRequest);

        ArrangementActData actData = getArrangementActData(actRequest.getArragementId());
        AccessToolRobot robot = getAccessToolRobot(actData.getAccessTool());
        AccessToolRobot robotPS = (robot.getType() == AccessToolRobotType.PS ? robot : new AccessToolRobot());
        AccessToolRobot robotPASD = (robot.getType() == AccessToolRobotType.PASD ? robot : new AccessToolRobot());

        ActRequestPPP aReq = new ActRequestPPP();
        aReq.setMissionId(getExternalMissionId(actData.getMissionId()));
        aReq.setArrangementId(actRequest.getArragementId());
        aReq.setPsId(robotPS.getOrigId());
        aReq.setPasdId(robotPASD.getOrigId());
        aReq.setPsName(robotPS.getOrigName());
        aReq.setPasdName(robotPASD.getOrigName());
        aReq.setStartDate(actRequest.getStartDate());
        aReq.setEndDate(actRequest.getEndDate());

        List<ActCheckResult> actCheckResults = dispatcherWebClient.getActCheckResults(actRequest.getArragementId());
        log.info("Получены результаты выполнения мероприятия для акта: ID мероприятия {}, количество: {}",
                actRequest.getArragementId(),
                actCheckResults.size());
        List<ActCheckResultPPP> checkResults = new ArrayList<>();
        for (ActCheckResult actCheckResult : actCheckResults){
            ActCheckResultPPP achRes = new ActCheckResultPPP();
            achRes.setCheckResultId(actCheckResult.getCheckResultId());
            achRes.setCheckUnitType(actCheckResult.getCheckUnitType().name());
            achRes.setCheckUnitValue(actCheckResult.getCheckUnitValue());
            achRes.setDate(actCheckResult.getDate());
            checkResults.add(achRes);
        }
        aReq.setCheckResults(checkResults);

        List<String> screenshotes = dispatcherWebClient.getActScreenshotesBase64(actRequest.getArragementId());
        log.info("Получены скриншоты выполнения мероприятия для акта: ID мероприятия {}, количество: {}",
                actRequest.getArragementId(),
                screenshotes.size());
        try{
            return send(aReq, screenshotes);
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseStatusString send(ActRequestPPP actRequest, List<String> screenShots) throws JsonProcessingException {
        log.info("Отправка сформированного акта в ППП: {}", actRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(actRequest);

        MultiValueMap<String, String> jsonMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder( "form-data")
                .name("jsonData")
                .build();
        jsonMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        jsonMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> jsonDataEntity = new HttpEntity<>(jsonData, jsonMap);

        // тело
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("jsonData", jsonDataEntity);

        for (int i = 0; i < screenShots.size(); i++){
            String screen = screenShots.get(i).trim();
            byte[] bytes = StringUtils.isEmpty(screen) ? new byte[]{} : Base64.getDecoder().decode(screen);
            String name = "images["+i+"]";

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            ContentDisposition cd = ContentDisposition
                    .builder( "form-data")
                    .name(name)
                    .build();
            map.add(HttpHeaders.CONTENT_DISPOSITION, cd.toString());
            map.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
            HttpEntity<byte[]> imgEntity = new HttpEntity<>(bytes, map);
            body.add(name, imgEntity);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<ResponseStatusString> response =
                anonymizerRestTemplate.exchange(
                        UriComponentsBuilder.fromHttpUrl(baseUrl).path("/ArragementReport/").build().toString(),
                        HttpMethod.POST,
                        requestEntity,
                        ResponseStatusString.class);

        ResponseStatusString responseStatus = response.getBody();
        HttpStatus httpStatus = response.getStatusCode();
        int code = httpStatus.value();

        responseStatus = responseStatus != null ? responseStatus :
                new ResponseStatusString(false, httpStatus.toString());

        log.info("Акт отправлен в ППП. Http-статус: {}, ответ: {}, данные акта: {}",
                httpStatus.toString(),
                responseStatus,
                actRequest);

        if (!responseStatus.isStatus())
            throw new AS_15_8_POD_Exception("Ошибка отправки акта в ППП РА: " + responseStatus.getResponse());

        try {
            notifyActConfirmed(actRequest.getArrangementId());
        }
        catch(Exception ee){
            log.error("Ошибка отпрвки уведомления в Dispatcher. ArrangementId = " + actRequest.getArrangementId());
            ee.printStackTrace();
        }

        return responseStatus;
    }

    private void notifyActConfirmed(Long arrangementId){
        log.info("Отправка в PPT уведомления об успшном подтверждении акта в ППП Анонимайзере, arrangementId = {}" + arrangementId);
        restTemplate.getForObject(
                UriComponentsBuilder
                        .fromHttpUrl(gatewayUrl)
                        .path("/ppt/arrangements/confirm_success_sent")
                        .queryParam("arrangementId", arrangementId)
                        .build().toString(),
                String.class
        );
    }

    private AccessToolRobot getAccessToolRobot(String accessTool){
        AccessToolRobot[] accessToolRobots = restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(gatewayUrl).path("/config/access_tool_id")
                        .queryParam("name", accessTool)
                        .build().toString(),
                new HttpEntity<>(new HttpHeaders()),
                AccessToolRobot[].class);

        if (accessToolRobots == null || accessToolRobots.length == 0)
            throw new AS_15_8_POD_Exception("Robot не найден по accessTool = " + accessTool);

        return accessToolRobots[0];
    }

    private ArrangementActData getArrangementActData(Long arrangementId){
        ArrangementActData arrangementActData = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(gatewayUrl).path("/ppt/arrangements/act_data")
                        .queryParam("arrangement_id", arrangementId)
                        .build().toString(),
                ArrangementActData.class);

        if (arrangementActData == null)
            throw new AS_15_8_POD_Exception("Данные мероприятия не найдены по arrangement ID = " + arrangementId);

        return arrangementActData;
    }

    private Long getExternalMissionId(Long id) {
        if (id == null)
            return null;

        Long externalMissionId = missionRepository.getOriginId(id);
        if (externalMissionId == null)
            throw new AS_15_8_POD_Exception("externalMission не найден по mission ID = " + id);

        return externalMissionId;
    }

}
