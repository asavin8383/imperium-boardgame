package restapi;


import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ExceptionErdiLoad;
import exceptions.ExceptionErdiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.response.*;
import model.rest.ContentRest;
import model.rest.control.PodState;
import model.scheme.AddonVersion;
import model.scheme.ContentVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import parsers.ErdiFullParser;
import repositories.AddonVersionRepository;
import repositories.ContentVersionRepository;
import repositories.impl.ParameterRepositoryExtend;
import services.ErdiLoaderService;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErdiRestClient {

    private final RestTemplate registryAnonimyzersRestTemplate;
    private final ErdiLoaderService erdiLoaderService;
    private final ParameterRepositoryExtend parameterRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final AddonVersionRepository addonVersionRepository;
    private final SubTypeRestClient subTypeRestClient;
    private final AddonRestClient addonRestClient;
    private final JdbcTemplate jdbcTemplate;

    private static final String urlRest = "";
    private static final String tempDir = "temp_dir";

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    private boolean isError = false;
    private boolean isLoading = false;
    private String errorMessage = "";
    private String stateDetails = "";


    public boolean getIsLoading(){
        return isLoading;
    }

    public String getUpdateDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateUpdate = getActualContentDate();
        return dateUpdate == null ? "" : dateFormat.format(dateUpdate);
    }

    public PodState getLoadState() throws ParseException {
        ResponseEntity<RestResponseDumpDate> entity = registryAnonimyzersRestTemplate.exchange(
                baseUrl + "/getLastDumpDate/",
                HttpMethod.GET, null, RestResponseDumpDate.class);

        RestResponseDumpDate resp = entity.getBody();
        Date dateDumpDate = (resp == null ? null : resp.getDumpDate());
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT"));

        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        AddonVersion lastAddonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();

        Date dateUpdate = getActualContentDate();
        String strDateUpdate = dateUpdate == null ? "" : dateFormat1.format(dateUpdate);

        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String actualServerDumpDate = (dateDumpDate == null ? "" : dateFormat2.format(dateDumpDate));

        String state = getState();
        String errorMessage = getErrorMessage();
        String stateDetails = getStateDetails();

        List<ContentVersion> allContents = contentVersionRepository
                .findAll(Sort.by(Sort.Order.asc("id")));
        List<Long> allContentsInt = allContents.stream()
                .map(contentVersion -> contentVersion.getId())
                .collect(Collectors.toList());

        PodState podState = new PodState(strDateUpdate, actualServerDumpDate, state, errorMessage, stateDetails,
                lastContentVersion == null ? null : lastContentVersion.getId(),
                lastAddonVersion == null ? null : lastAddonVersion.getId(),
                allContentsInt);
        return podState;
    }

    public void startUpdateErdi(){
        if (isLoading)
            return;
        isLoading = true;

        try{
            log.info("====== Начало обновления справочников");
            loadFullERDI();
            loadAllDeltaERDI();
            loadSybTypes();
            loadAddons();
            refreshViews();
            log.info("====== Конец обновления справочников");
        }
        catch(Exception ex){
            log.error("Ошибка обновлении справочников", ex);
            throw new CompletionException(ex);
        }
        finally {
            isLoading = false;
        }
    }

    public void removeVersionTo(int version){
        CompletableFuture
                .runAsync(() -> {
                    while(true){
                        ContentVersion last = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
                        if (last == null || version >= last.getId())
                            return;
                        removeLastContentVersion();
                    }
                });
    }


    public void removeLastContentVersion() {
        errorMessage = "";
        isError = false;

        Long id = null;
        try {
            ContentVersion last = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
            id = (last == null ? null : last.getId());

            stateDetails = "Удаление последней контент версии: " + id;
            erdiLoaderService.removeLastContentVersion();
        }
        catch (Exception e){
            isError = true;
            errorMessage = "Удаление последней версии завершилось с ошибкой";
        }
        finally {
            stateDetails = isError ? errorMessage : "Удаление версии контента " + id + " прошло успешно!";
        }
    }

    public String getState(){
        if (isLoading)
            return "PROCESS";

        return (isError ? "ERROR" : "");
    }

    public String getStateDetails(){
        return stateDetails;
    }

    public String getErrorMessage(){
        return isError ? errorMessage : "";
    }

    public void loadSybTypes() {
        log.info("Загрузка картотеки Справочник нарушений");
        subTypeRestClient.readFromNet();
    }

    public void loadFullERDI() throws IOException, ExceptionErdiParser {
        isError = false;
        errorMessage = "";

        boolean deleteTempFile = false;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd__hh_mm_ss");
        Path path = Paths.get(tempDir, String.format("full_erdi_%s.zip", dateFormat.format(new Date())));

        try{
            ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
            boolean isFullErdi = (fullContentVersion != null);

            if (isFullErdi){
                return;
            }

            log.info("---> Запуск загрузки полного справочника ЕРДИ");

            if (deleteTempFile) {
                Files.deleteIfExists(path);
            }

            File directory = path.toFile().getParentFile();
            if (!directory.exists()){
                directory.mkdirs();
            }

            stateDetails = "Загрузка полного справочника ЕРДИ";
            getAndSaveFullErdi(path);

            stateDetails = "Парсинг и заливка полного справочника ЕРДИ в БД";

            //path = Paths.get(tempDir, "mytest.zip");
            erdiToDB(path, null);
        }
        catch (Exception e){
            errorMessage = "Ошибка загрузки полного справочника ЕРДИ";
            isError = true;
            throw e;
        }
        finally {
            stateDetails = isError ? errorMessage : "Загрузка полного справочника ЕРДИ прошла успешно";
            if (deleteTempFile){
                try{
                    Files.deleteIfExists(path);
                }
                catch (IOException ie){}
            }
        }
    }

    public void loadAddons() {
        AddonVersion fullAddonVersion = addonVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
        boolean isFullAddons = fullAddonVersion != null;

        if (!isFullAddons){
            System.out.println("Загрузка полного справочнка аддонов");
            addonRestClient.readFullFromNet();
        }

        System.out.println("Загрузка дельт аддонов");
        List<DeltaAddonEntry> list = addonRestClient.readDeltaList();
        if (list.size() > 0) {
            for (DeltaAddonEntry deltaAddonEntry : list){
                System.out.println("Загрузка дельты аддона: " + deltaAddonEntry.toString());
                addonRestClient.readDeltaFromNet(deltaAddonEntry.getDeltaId(),
                        deltaAddonEntry.getActualDate());
            }
        }
    }

    private void refreshViews(){
        log.info("---> Установка типов ИРТЗ и Обновление MATERIALIZED VIEWS");
        jdbcTemplate.execute("select sor.set_irtz_type()");
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW sor.check_units");
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW sor.content_view");
    }


    private void getAndSaveFullErdi(Path path) throws IOException, RestClientException {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getFullERDI/")
                        .build()
                        .expand(baseUrl);

        System.out.println("----> getting full ERDI from service: " + uriComponents.toString());

        ResponseEntity<byte[]> entity = registryAnonimyzersRestTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );

        Files.write(path, entity.getBody());
        System.out.println("----> full ERDI was write to file: " + path.toString());
    }

    private void getAndSaveDeltaErdi(Path path, DeltaIdEntry deltaIdEntry) throws IOException, RestClientException {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaByDeltaId/{deltaId}/")
                        .build()
                        .expand(baseUrl, deltaIdEntry.deltaId);

        System.out.println("----> getting delta ERDI from service: " + uriComponents.toString());

        ResponseEntity<byte[]> entity = registryAnonimyzersRestTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );

        Files.write(path, entity.getBody());
        System.out.println("----> delta ERDI was write to file: " + path.toString());
    }

    public void loadAllDeltaERDI() throws ExceptionErdiParser {
        isError = false;
        errorMessage = "";

        int count = 0;
        try{
            List<DeltaIdEntry> deltaList = getLastDumpDeltaListByDate();
            count = deltaList.size();

            log.info("Получен список дельт: размер=" + count);
            log.info(deltaList.toString());

            if (count > 0){
                log.info("---> Запуск загрузки дельт ЕРДИ");

                for(DeltaIdEntry deltaIdEntry : deltaList){
                    loadDeltaERDI(deltaIdEntry);
                    if (isError)
                        throw new ExceptionErdiParser("Error load delta");
                }
            }
        }
        catch (Exception e){
            errorMessage = StringUtils.isEmpty(errorMessage) ? "Ошибка загрузки дельта ЕРДИ" : errorMessage;
            isError = true;
            throw new ExceptionErdiParser(e);
        }
        finally {
            stateDetails = isError ? errorMessage : "Загрузка дельт (" + count + ") ЕРДИ прошла успешно";
        }
    }

    public Date getActualContentDate(){
        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        Date actualDate = lastContentVersion == null ? null : (lastContentVersion.getRegUpdateTime() != null ?
                lastContentVersion.getRegUpdateTime() : lastContentVersion.getDeltaUpdateTime());
        return actualDate;
    }

    public List<DeltaIdEntry> getLastDumpDeltaListByDate() throws ParseException {
        Date dateUpdate = getActualContentDate();
        System.out.println("dateUpdate = " + dateUpdate);

        if (dateUpdate == null){
            return new ArrayList<>();
        }
        return getDumpDeltaListByDate(dateUpdate);
    }

    @Transactional
    public String setParameter(String name, String value) {
        try{
            parameterRepository.setParameterValue(name, value);
        }
        catch(Exception e){
            e.printStackTrace();
            return "НЕ удалось обновить параметр! Ошибка: " + e.getMessage();
        }

        return "Парамет " + name + "=" + value + " успешно обновлен";
    }

    public List<DeltaIdEntry> getDumpDeltaListByDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateStr = dateFormat.format(date);

        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaListByDate/{dateStr}/")
                        .build()
                        .expand(baseUrl, dateStr);

        System.out.println(uriComponents.toString());

        ResponseEntity<RestResponseDeltaListByDate> entity;
        try {
            log.info("Получение списка дельт: {} по дате: {}", uriComponents.toString(), dateStr);
            entity = registryAnonimyzersRestTemplate.exchange(
                    uriComponents.toString(),
                    HttpMethod.GET,
                    null,
                    RestResponseDeltaListByDate.class
            );
        }
        catch (Throwable e){
            log.error("Ошибка загрузки списка дельт по дате: {}", dateStr);
            log.error(errorMessage);
            return new ArrayList<>();
        }

        RestResponseDeltaListByDate resp = entity.getBody();
        SimpleDateFormat deltaDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        List<DeltaIdEntry> list = resp.response;
        list = list.stream()
                .filter(deltaIdEntry -> {
                    try {
                        Date deltaDate = deltaDateFormat.parse(deltaIdEntry.actualDate);
                        if (deltaDate.before(date))
                            return false;
                    }
                    catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return deltaIdEntry.isEmpty.equals("0");
                })
                .collect(Collectors.toList());

        return list;
    }

    public void loadDeltaERDI(DeltaIdEntry deltaIdEntry) throws IOException, ExceptionErdiParser {
        stateDetails = "Загрузка дельты ЕРДИ: " + deltaIdEntry.deltaId;

        boolean deleteTempFile = false;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd__hh_mm_ss");
        Path path = Paths.get(tempDir, String.format("erdi_delta_%s_%s.zip", deltaIdEntry.deltaId, dateFormat.format(new Date())));

        try{
            if (deleteTempFile) {
                Files.deleteIfExists(path);
            }

            File directory = path.toFile().getParentFile();
            if (!directory.exists()){
                directory.mkdirs();
            }

            getAndSaveDeltaErdi(path, deltaIdEntry);
            erdiToDB(path, deltaIdEntry);
        }
        catch (Exception e){
            errorMessage = "Ошибка загрузки дельты ЕРДИ " + deltaIdEntry.deltaId;
            throw e;
        }
        finally {
            if (deleteTempFile){
                try{
                    Files.deleteIfExists(path);
                }
                catch (IOException ie){}
            }
        }
    }


    private void erdiToDB(Path path, DeltaIdEntry deltaIdEntry) throws IOException, ExceptionErdiParser {
        log.info("Попытка парсинга {}, file: {}", deltaIdEntry != null ? "Delta ERDI" : "Full ERDI", path.toAbsolutePath().toString());

        ZipFile zipFile = null;

        try{
            zipFile = new ZipFile(path.toFile());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                String entryName = (deltaIdEntry != null ? "dump_delta.xml" : "dump.xml");

                if (entry.getName().equals(entryName)){
                    InputStream stream = zipFile.getInputStream(entry);
                    ErdiFullParser parser = new ErdiFullParser();

                    List<ContentRest> allContents = new ArrayList<>();
                    log.info("Начат парсинг " + entryName + "...");
                    stateDetails = "Парсинг ЕРДИ";

                    parser.parse(stream, (register, contents) -> {
                        allContents.addAll(contents);
                        log.info("parsing count... " + allContents.size());
                        stateDetails = "Парсинг ЕРДИ (" + allContents.size() + ")";

                        if (contents.size() == 0){
                            try {
                                stateDetails = "Заливка справочника ЕРДИ в БД";
                                erdiLoaderService.fillContents(deltaIdEntry, register, allContents);
                            } catch (ExceptionErdiLoad exceptionErdiLoad) {
                                exceptionErdiLoad.printStackTrace();
                                isError = true;
                            }
                            return false;
                        }
                        return true;
                    });
                }
            }
        }
        catch (ZipException ze){
            if (checkDeltaErdiNotFound(path)){
                System.out.println("Проферка файла ЕРДИ: пустой контент! Корректная ситуация.");
                /* ignore exception */
            }
            else{
                throw ze;
            }
        }
        finally {
            try {
                if (zipFile != null)
                    zipFile.close();
            }
            catch (Exception ee){}
        }
    }

    private boolean checkDeltaErdiNotFound(Path path){
        File f = path.toFile();
        if (f.length() >= 2048)
            return false;

        try {
            String content = new String(Files.readAllBytes(path));
            ObjectMapper mapper = new ObjectMapper();
            RestResponseStatusString restResponse = mapper.readValue(content, RestResponseStatusString.class);
            return !StringUtils.isEmpty(restResponse.response) && restResponse.response.equalsIgnoreCase("Not found");
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}


