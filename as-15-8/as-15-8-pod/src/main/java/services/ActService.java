package services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.AS_15_8_POD_Exception;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.rest.control.*;
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
import reactor.core.publisher.Flux;
import repositories.ContentRepository;
import repositories.MissionRepository;
import rest.*;
import webClient.DispatcherWebClient;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ActService {

    private final RestTemplate anonymizerRestTemplate;
    private final OAuth2RestTemplate restTemplate;
    private final MissionRepository missionRepository;
    private final DispatcherWebClient dispatcherWebClient;
    private final ContentRepository contentRepository;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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

        Flux<List<ActCheckResult>> actCheckResultsFlux = dispatcherWebClient.getActCheckResults(actRequest.getArragementId());
        List<ActCheckResult> actCheckResults = actCheckResultsFlux.toStream().flatMap(List::stream).collect(Collectors.toList());

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
            achRes.setForbiddenContentDetected(actCheckResult.isForbiddenContentDetected());
            try {
                List<ActCheckResultPodInfo> infos = contentRepository.findActCheckResultPodInfo(actCheckResult.getContentId());
                if(infos.size() > 0) {
                    ActCheckResultPodInfo actCheckResultPodInfo = infos.get(0);
                    achRes.setContentId(""+actCheckResultPodInfo.getErdiId());
                    achRes.setIncludeTime(dateFormat.format(actCheckResultPodInfo.getIncludeTime()));
                }
            } catch (Exception ex) {}
            /*
            if (StringUtils.isEmpty(actCheckResult.getDate())){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                achRes.setDate(dateFormat.format(new Date()));
            }*/

            checkResults.add(achRes);
        }
        aReq.setCheckResults(checkResults);

        Flux<List<ActAttachment>> attachmentsFlux = dispatcherWebClient.getActAttachments(actRequest.getArragementId());
        List<ActAttachment> attachments = attachmentsFlux.toStream().flatMap(List::stream).collect(Collectors.toList());

        log.info("Получены скриншоты выполнения мероприятия для акта: ID мероприятия {}, количество: {}",
                actRequest.getArragementId(),
                attachments.size());
        try{
            return send(aReq, attachments);
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    private ResponseStatusString send(ActRequestPPP actRequest, List<ActAttachment> attachments) throws JsonProcessingException {
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

        for (ActAttachment attachment : attachments) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            String name = ""; //
            if(attachment.getType().equals(ActAttachment.ActAttachmentType.SCREENSHOT)){
                name = "images";
            } else {
                name = "texts";
            }
            name+= "["+attachment.getId().toString()+"]";

            ContentDisposition cd = ContentDisposition
                    .builder("form-data")
                    .name(name)
                    .build();
            map.add(HttpHeaders.CONTENT_DISPOSITION, cd.toString());

            HttpEntity httpEntity;
            if (attachment.getType().equals(ActAttachment.ActAttachmentType.SCREENSHOT)) {
                byte[] bytes = StringUtils.isEmpty(attachment.getValue()) ? new byte[]{} : Base64.getDecoder().decode(attachment.getValue().trim());
                map.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
                httpEntity = new HttpEntity<>(bytes, map);
            } else {
                map.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                map.add(HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.displayName());
                httpEntity = new HttpEntity<>(attachment.getValue(), map);
                body.add(name, httpEntity);
            }
            body.add(name, httpEntity);
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
            log.error("Ошибка отпрвки уведомления в подсистему PPT. ArrangementId = " + actRequest.getArrangementId());
            ee.printStackTrace();
        }

        return responseStatus;
    }

    private void notifyActConfirmed(Long arrangementId){
        log.info("Отправка в PPT уведомления об успшном подтверждении акта в ППП Анонимайзере, arrangementId = {}", arrangementId);
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
