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
import org.springframework.web.util.UriComponentsBuilder;
import repositories.MissionRepository;
import rest.ActCheckResult;
import rest.ActRequest;
import rest.ResponseStatusString;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActService {

    private final OAuth2RestTemplate restTemplate;
    private final MissionRepository missionRepository;

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    @Value("${gateway.url}")
    private String gatewayUrl;


    @SneakyThrows
    public ResponseStatusString createAct(ActRequest actRequest) {
        log.info("Запрос на отправку акта в ППП: {}. Кол-во результатов: {}. Кол-во скриншотов: {}",
                actRequest,
                actRequest.checkResults.size(),
                actRequest.topScreenShots.size());

        AccessToolRobot robot = getAccessToolRobot(actRequest.arragementId);
        AccessToolRobot robotPS = (robot.type == AccessToolRobotType.PS ? robot : new AccessToolRobot());
        AccessToolRobot robotPASD = (robot.type == AccessToolRobotType.PASD ? robot : new AccessToolRobot());

        ActRequestPPP aReq = new ActRequestPPP();
        aReq.MissionId = getExternalMissionIdByArrangementId(actRequest.arragementId);
        aReq.ArragementId = actRequest.arragementId;
        aReq.PSId = robotPS.orig_id;
        aReq.PASDId = robotPASD.orig_id;
        aReq.PSName = robotPS.origName;
        aReq.PASDName = robotPASD.origName;
        aReq.StartDate = actRequest.startDate;
        aReq.EndDate = actRequest.endDate;

        List<ActCheckResultPPP> checkResults = new ArrayList<>();
        for (ActCheckResult actCheckResult : actRequest.checkResults){
            ActCheckResultPPP achRes = new ActCheckResultPPP();
            achRes.CheckResultId = actCheckResult.checkResultId;
            achRes.CheckUnitType = actCheckResult.checkUnitType.name();
            achRes.CheckUnitValue = actCheckResult.checkUnitValue;
            achRes.Date = actCheckResult.date;
            checkResults.add(achRes);
        }
        aReq.CheckResults = checkResults;

        try{
            return send(aReq, actRequest.topScreenShots);
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
            String screen = screenShots.get(i);
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
                restTemplate.exchange(
                        UriComponentsBuilder.fromHttpUrl(baseUrl).path("/ArragementReport/").build().toString(),
                        HttpMethod.POST,
                        requestEntity,
                        ResponseStatusString.class);

        ResponseStatusString responseStatus = response.getBody();
        HttpStatus httpStatus = response.getStatusCode();
        int code = httpStatus.value();

        responseStatus = responseStatus != null ? responseStatus :
                new ResponseStatusString((code >= 200 && code < 300) || code == 302, null);

        responseStatus.response = responseStatus.response != null ? responseStatus.response : httpStatus.toString();

        log.info("Акт отправлен в ППП. Http-статус: {}, ответ: {}, данные акта: {}",
                httpStatus.toString(),
                responseStatus,
                actRequest);

        return responseStatus;
    }


    private String getAccessTool(Long arrangementId){
        String accessTool = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(gatewayUrl).path("/ppt/arrangements/access_tool")
                        .queryParam("arrangement_id", arrangementId)
                        .build().toString(),
                String.class);

        if (StringUtils.isEmpty(accessTool))
            throw new AS_15_8_POD_Exception("accessTool не найден по arrangement ID = " + arrangementId);

        return accessTool;
    }

    private AccessToolRobot getAccessToolRobot(Long arrangementId){
        String accessTool = getAccessTool(arrangementId);

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

    private Long getExternalMissionIdByArrangementId(Long arrangementId) {
        Long missionId = getMissionId(arrangementId);
        return getExternalMissionId(missionId);
    }

    private Long getMissionId(Long arrangementId){
        Long missionId = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(gatewayUrl).path("/ppt/arrangements/mission_id")
                        .queryParam("arrangement_id", arrangementId)
                        .build().toString(),
                Long.class);

        if (missionId == null)
            throw new AS_15_8_POD_Exception("mission не найден по arrangement ID = " + arrangementId);

        return missionId;
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
