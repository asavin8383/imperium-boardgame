package services;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.AS_15_8_POD_Exception;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.response.MissionEntry;
import model.response.RestResponseMissions;
import model.response.RestResponseStatusString;
import model.scheme.Mission;
import model.scheme.MissionAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.MissionAttachmentRepo;
import repositories.MissionRepository;
import rest.MissionData;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class MissionService {

    private final OAuth2RestTemplate oAuth2RestTemplate;
    private final RestTemplate anonymizerRestTemplate;
    private final MissionRepository missionRepository;
    private final MissionAttachmentRepo missionAttachmentRepo;

    private boolean stateLoading = false;

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @SneakyThrows
    public void fillMissions() {
        fillMissionsWithConfirm(true);
    }

    @SneakyThrows
    public void fillMissionsWithConfirm(boolean confirm) {
        if (stateLoading){
            log.info("Загрузка поручений уже проводится в данный момент.");
            return;
        }

        stateLoading = true;        // todo - не хватает потокобезопасности!
        try{
            log.info("Старт загрузки списка попручений");
            List<MissionEntry> missions = getMissionsFrom();
            log.info("Список поручений: {}", missions);

            boolean isError = false;
            if (!missions.isEmpty()){
                for(MissionEntry missionEntry : missions){
                    try {
                        saveMissionWithConfirm(missionEntry, confirm);
                    }
                    catch(Exception e){
                        isError = true;
                        log.error("Ошибка сохранения поручения: " + missionEntry.toString());
                        e.printStackTrace();
                    }
                }
            }
            log.info("Загрузка поручений завершена {}", isError ? "с ошибками" : "успешно");
        }
        catch (Exception e){
            throw new AS_15_8_POD_Exception("Ошибка загрузки поручений!", e);
        }
        finally{
            stateLoading = false;
        }
    }

    @Transactional
    public void saveMissionWithConfirm(MissionEntry missionEntry, boolean confirm) throws IOException, ParseException {
        Mission mission = missionRepository.findByOrigId(missionEntry.getId());
        if (mission != null){
            log.info("Поручение уже есть в БД: " + missionEntry.toString());
        }
        else {
            log.info("Добавляем поручение в БД: " + missionEntry.toString());
            mission = missionRepository.save(createMission(missionEntry));
            MissionAttachment missionAttachment = new MissionAttachment();
            missionAttachment.setMission(mission);
            missionAttachment.setAttachment(missionEntry.getDocFileDataBytes());
            missionAttachmentRepo.save(missionAttachment);
        }

        sendMissionDataToPPT(mission);

        if (confirm){
            confirmMission(missionEntry);
        }
    }

    private void sendMissionDataToPPT(Mission mission){
        MissionData missionData = new MissionData(mission.getId(), "Поручение " + mission.getDocNum());
        log.info("Отправка поручения в PPT: " + missionData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MissionData> entity = new HttpEntity<>(missionData, headers);
        oAuth2RestTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(gatewayUrl).path("/ppt/formal_tasks/create_with_mission").build().toString(),
                entity, ResponseEntity.class);
    }

    public byte[] receiveMissionDocumentFromDB(long id){
        return missionAttachmentRepo.getOne(id).getAttachment();
    }

    private byte[] mapPdf(ResultSet rs, int rowNum) throws SQLException {
        LobHandler lobHandler = new DefaultLobHandler();
        return lobHandler.getBlobAsBytes(rs,1);
    }

    private Mission createMission(MissionEntry missionEntry) throws ParseException, UnsupportedEncodingException {
        Mission mission = new Mission();
        mission.setOrigId(missionEntry.getId());
        mission.setDocNum(missionEntry.docNum);
        mission.setTypeCheck(missionEntry.typeCheck);
        mission.setDateApproved(missionEntry.getDateApprovedDate());
        return mission;
    }

    private List<MissionEntry> getMissionsFrom() throws IOException, RestClientException {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getMissionsList/")
                        .build()
                        .expand(baseUrl);

        log.info("----> getting missions from service: " + uriComponents.toString());

        ResponseEntity<byte[]> entity = anonymizerRestTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );
        byte[] data = entity.getBody();

        RestResponseStatusString restResponseStatusString =
                getResponseStatusString(data);

        if (restResponseStatusString == null){
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
            RestResponseMissions responseMissions = mapper.readValue(data, RestResponseMissions.class);
            return responseMissions.getMissionList();
        }
        else {
            log.info("Статус ответа: " + restResponseStatusString.response);
            return new ArrayList<>();
        }
    }

    private void confirmMission(MissionEntry missionEntry) throws IOException, RestClientException {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/confirmAcceptMission/{id}/")
                        .build()
                        .expand(baseUrl, missionEntry.id);

        log.info("----> confirmAcceptMission to service: " + uriComponents.toString());

        ResponseEntity<RestResponseStatusString> entity = anonymizerRestTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                RestResponseStatusString.class
        );
        RestResponseStatusString status = entity.getBody();

        log.info("Результат подтверждения поручения: " + (status == null ? null : status.toString()));
    }

    private RestResponseStatusString getResponseStatusString(byte[] data){
        if (data == null || data.length > 2048)
            return null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(data, RestResponseStatusString.class);
        }
        catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

}
