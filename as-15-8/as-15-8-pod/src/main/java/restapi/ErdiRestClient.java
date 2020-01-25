package restapi;


import exceptions.AS_15_8_POD_Exception;
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
import java.util.concurrent.atomic.AtomicReference;
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
    private final RestClientUtils restClientUtils;
    private final AddonRestClient addonRestClient;
    private final JdbcTemplate jdbcTemplate;

    private static final String ENTITY_DELTA_NAME = "dump_delta.xml";
    private static final String ENTITY_FULL_NAME = "dump.xml";

    private static final String urlRest = "";
    private static final String tempDir = "temp_dir";

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    private boolean isError = false;
    private boolean isLoading = false;
    private String errorMessage = "";
    private String stateDetails = "";

    private Date lastTimeUpdateViews = null;
    private static final long DELAY_UPDATE_VIEWS_MS = 60*60*1000;


    public boolean getIsLoading(){
        return isLoading;
    }

    public String getUpdateDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
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
        //dateFormat1.setTimeZone(TimeZone.getTimeZone("GMT"));

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

            boolean wereSybTypeChanges = loadSybTypes();

            boolean wasLoadedFullERDI = loadFullERDI();
            boolean wasLoadedFullAddons = loadFullAddons();

            int countLoadedDeltaERDI = loadAllDeltaERDI();
            int countLoadedAddons = loadAllDeltaAddons();

            boolean needUpdateViews =
                    wereSybTypeChanges ||
                    wasLoadedFullERDI || wasLoadedFullAddons ||
                    countLoadedDeltaERDI > 0 || countLoadedAddons > 0;
            refreshViews(needUpdateViews);

            log.info("====== Конец обновления справочников");
        }
        catch(Exception ex){
            log.error("Ошибка обновлении справочников!", ex);
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

    public boolean loadSybTypes() {
        log.info("Загрузка картотеки Справочник нарушений");
        return subTypeRestClient.readFromNetDiff();
    }

    /**
     *
     * @return true - загрузка полного справочника ЕРДИ прошла. false - загрузка не требуется.
     * @throws IOException
     * @throws ExceptionErdiParser
     */
    public boolean loadFullERDI() throws IOException, ExceptionErdiParser {
        isError = false;
        errorMessage = "";

        boolean deleteTempFile = true;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd__hh_mm_ss");
        Path path = Paths.get(tempDir, String.format("full_erdi_%s.zip", dateFormat.format(new Date())));

        try{
            ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
            boolean isFullErdi = (fullContentVersion != null);

            if (isFullErdi){
                return false;
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

            return true;
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

    /**
     * @return true - загрузка полного српвочника аддонов прошла. false - не прошла
     *
     */
    public boolean loadFullAddons() {
        AddonVersion fullAddonVersion = addonVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
        boolean isFullAddons = fullAddonVersion != null;

        if (!isFullAddons){
            ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
            if (fullContentVersion == null)
                throw new AS_15_8_POD_Exception("Ошибка при загрузке полного справочника аддонов. Не найдена запись загрузки полного ЕРДИ!");

            System.out.println("Загрузка полного справочнка аддонов");
            addonRestClient.readFullFromNet(fullContentVersion.getRegUpdateTime()); // date - дата полной загрузки ЕРДИ
        }

        return !isFullAddons;
    }

    public int loadAllDeltaAddons() {
        System.out.println("Загрузка дельт аддонов");
        List<DeltaAddonEntry> list = addonRestClient.readDeltaList();
        if (list.size() > 0) {
            int i=0;
            for (DeltaAddonEntry deltaAddonEntry : list){
                log.info("Загрузка дельты аддона {}/{}:", ++i, list.size(), deltaAddonEntry.toString());
                addonRestClient.readDeltaFromNet(deltaAddonEntry.getDeltaId(),
                        deltaAddonEntry.getActualDate());
            }
        }
        return list.size();
    }

    private void refreshViews(boolean forceRefresh){
        if (!forceRefresh && lastTimeUpdateViews != null &&
                lastTimeUpdateViews.getTime() + DELAY_UPDATE_VIEWS_MS > (new Date()).getTime()){
            return;
        }
        lastTimeUpdateViews = new Date();

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

    /**
     *
     * @return Результат - кол-во успешно загруженных дельт
     * @throws ExceptionErdiParser
     */
    public int loadAllDeltaERDI() throws ExceptionErdiParser {
        isError = false;
        errorMessage = "";

        int count = 0;
        DeltaIdEntry curDeltaIdEntry = null;
        try{
            List<DeltaIdEntry> deltaList = getActualErdiDeltaEntries();
            count = deltaList.size();

            log.info("Получен список дельт. Размер = {}", count);
            log.info("Список дельт: {}", deltaList.stream().map(d -> d.deltaId).collect(Collectors.toList()).toString());

            if (count > 0){
                log.info("---> Запуск загрузки дельт ЕРДИ");

                int i=0;
                for(DeltaIdEntry deltaIdEntry : deltaList){
                    log.info("--> загрузка дельты id={}, {}/{}", deltaIdEntry.deltaId, ++i, count);
                    curDeltaIdEntry = deltaIdEntry;
                    loadDeltaERDI(deltaIdEntry);
                }
            }
        }
        catch (Exception e){
            errorMessage = StringUtils.isEmpty(errorMessage)
                    ? "Ошибка загрузки дельты ЕРДИ " + (curDeltaIdEntry == null ? "" : curDeltaIdEntry.toString())
                    : errorMessage;
            isError = true;
            throw new ExceptionErdiParser(errorMessage, e);
        }
        finally {
            stateDetails = isError ? errorMessage : "Загрузка дельт (" + count + ") ЕРДИ прошла успешно";
        }
        return count;
    }

    public Date getActualContentDate(){
        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        Date actualDate = lastContentVersion == null ? null : (lastContentVersion.getRegUpdateTime() != null ?
                lastContentVersion.getRegUpdateTime() : lastContentVersion.getDeltaUpdateTime());
        return actualDate;
    }

    public List<DeltaIdEntry> getActualErdiDeltaEntries() {
        Date dateUpdate = getActualContentDate();
        if (dateUpdate == null){
            return new ArrayList<>();
        }
        return restClientUtils.getDumpDeltaListByDate(dateUpdate);
    }


    public void loadDeltaERDI(DeltaIdEntry deltaIdEntry) throws IOException, ExceptionErdiParser {
        stateDetails = "Загрузка дельты ЕРДИ: " + deltaIdEntry.deltaId;

        boolean deleteTempFile = true;

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


    private void erdiToDB(Path path, DeltaIdEntry deltaIdEntry) throws AS_15_8_POD_Exception {
        log.info("Попытка парсинга {}, file: {}", deltaIdEntry != null ? "Delta ERDI" : "Full ERDI", path.toAbsolutePath().toString());

        AtomicReference<ExceptionErdiLoad> exception = new AtomicReference<>(null);

        String entryName = (deltaIdEntry != null ? ENTITY_DELTA_NAME : ENTITY_FULL_NAME);
        ZipFile zipFile = null;

        try{
            zipFile = new ZipFile(path.toFile());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();

                if (entry.getName().equals(entryName)){
                    InputStream stream = zipFile.getInputStream(entry);
                    ErdiFullParser parser = new ErdiFullParser();

                    //parser.setMaxContentSize(40000);  // todo - закомментировать

                    List<ContentRest> allContents = new ArrayList<>();
                    log.info("Начат парсинг " + entryName + "...");
                    stateDetails = "Парсинг ЕРДИ";

                    parser.parse(stream, (register, contents) -> {
                        if (allContents.size() > 0)
                            log.info("разобрано записей: {} ... ", allContents.size());

                        allContents.addAll(contents);
                        stateDetails = "Парсинг ЕРДИ (" + allContents.size() + ")";

                        if (contents.size() == 0){
                            try {
                                stateDetails = "Заливка справочника ЕРДИ в БД";
                                erdiLoaderService.fillContents(deltaIdEntry, register, allContents);
                            } catch (ExceptionErdiLoad exceptionErdiLoad) {
                                exceptionErdiLoad.printStackTrace();
                                exception.set(exceptionErdiLoad);
                            }
                            return false;
                        }
                        return true;
                    });
                    break;  // завершаем цикл
                }
            }

            if (exception.get() != null){
                throw exception.get();
            }
        }
        catch (ZipException ze){
            if (restClientUtils.checkDeltaErdiNotFound(path)){
                System.out.println("Проферка файла ЕРДИ: пустой контент! Корректная ситуация.");
                /* ignore exception */
            }
            else{
                isError = true;
                throw new AS_15_8_POD_Exception("Ошибка во время разбора файла полного ЕРДИ", ze);
            }
        }
        catch (Exception e) {
            isError = true;
            throw new AS_15_8_POD_Exception("Ошибка во время разбора файла полного ЕРДИ", e);
        }
        finally {
            try {
                if (zipFile != null)
                    zipFile.close();
            }
            catch (Exception ee){}
        }
    }

}


