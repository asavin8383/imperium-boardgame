package restapi;


import exceptions.AS_15_8_POD_Exception;
import exceptions.ExceptionErdiLoad;
import exceptions.ExceptionErdiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.response.*;
import model.rest.ContentRest;
import model.scheme.AddonVersion;
import model.scheme.ContentVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import services.ErdiLoaderService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private String errorMessage = "";
    private String stateDetails = "";

    private Date lastTimeUpdateViews = new Date();
    private static final long DELAY_UPDATE_VIEWS_MS = 4*60*60*1000;


    public boolean getIsLoading(){
        return isLoading.get();
    }

    public String getUpdateDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        //DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateUpdate = getActualContentDate();
        return dateUpdate == null ? "" : dateFormat.format(dateUpdate);
    }

    public void startUpdateErdi(){
        if (!isLoading.compareAndSet(false, true))
            return;

        errorMessage = "";
        stateDetails = "Старт обновления справочников";

        boolean wereSybTypeChanges = false;
        boolean wasLoadedFullERDI = false;
        boolean wasLoadedFullAddons = false;
        int countLoadedDeltaERDI = 0;
        int countLoadedAddons = 0;

        try{
            log.info("====== Начало обновления справочников");

            wereSybTypeChanges = loadSybTypes();

            wasLoadedFullERDI = loadFullERDI();
            wasLoadedFullAddons = loadFullAddons();

            countLoadedDeltaERDI = loadAllDeltaERDI();
            countLoadedAddons = loadAllDeltaAddons();

            boolean needUpdateViews =
                    wereSybTypeChanges ||
                    wasLoadedFullERDI || wasLoadedFullAddons ||
                    countLoadedDeltaERDI > 0 || countLoadedAddons > 0;
            refreshViews(needUpdateViews);

            stateDetails = String.format(
                    "Обновление справочников успешно завершено: " +
                            "изменения в справочнике нарушений - %s, " +
                            "загрузка полного справочника ЕРДИ - %s, " +
                            "загрузка полного спрвочника аддонов - %s, " +
                            "кол-во дельт ЕРДИ - %d, " +
                            "кол-во дельт аддонов - %d.",
                    wereSybTypeChanges ? "да" : "нет",
                    wasLoadedFullERDI ? "да" : "нет",
                    wasLoadedFullAddons ? "да" : "нет",
                    countLoadedDeltaERDI,
                    countLoadedAddons);

            log.info("====== Конец обновления справочников");
        }
        catch(Exception ex){
            errorMessage = !StringUtils.isEmpty(errorMessage) ? errorMessage :
                    "Обновление справочников завершилось с ошибкой! " + ex.getMessage();
            log.error(errorMessage, ex);
            throw new CompletionException(ex);
        }
        finally {
            isLoading.set(false);
        }
    }

    public String getStateDetails(){
        return stateDetails;
    }

    public String getErrorMessage(){
        return errorMessage;
    }

    public boolean loadSybTypes() {
        stateDetails = "Загрузка справочника нарушений";
        log.info(stateDetails);
        try {
            return subTypeRestClient.readFromNetDiff();
        }
        catch (Exception e){
            errorMessage = "Ошибка загрузки справочника нарушений: " + e.getMessage();
            throw e;
        }
    }

    /**
     *
     * @return true - загрузка полного справочника ЕРДИ прошла. false - загрузка не требуется.
     * @throws IOException
     */
    public boolean loadFullERDI() throws IOException {
        stateDetails = "Проверка необходимости загрузки полного справочника ЕРДИ";

        boolean deleteTempFile = true;

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd__hh_mm_ss");
        Path path = Paths.get(tempDir, String.format("full_erdi_%s.zip", dateFormat.format(new Date())));

        try{
            ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
            boolean isFullErdi = (fullContentVersion != null);

            if (isFullErdi){
                return false;
            }

            if (deleteTempFile) {
                Files.deleteIfExists(path);
            }

            File directory = path.toFile().getParentFile();
            if (!directory.exists()){
                directory.mkdirs();
            }

            stateDetails = "Загрузки полного справочника ЕРДИ";
            getAndSaveFullErdi(path);

            stateDetails = "Разбор и сохранение полного справочника ЕРДИ в БД";
            erdiToDB(path, null);

            return true;
        }
        catch (Exception e){
            errorMessage = "Ошибка загрузки и сохранения полного справочника ЕРДИ. " + e.getMessage();
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

    public void loadDeltaERDI(DeltaIdEntry deltaIdEntry) throws IOException, ExceptionErdiParser {
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
            errorMessage = "Ошибка при загрузке дельты ЕРДИ id=" + deltaIdEntry.deltaId;
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

    /**
     * @return true - загрузка полного српвочника аддонов прошла. false - не прошла
     *
     */
    public boolean loadFullAddons() {
        stateDetails = "Проверка необходимости загрузки полного справочника аддонов";

        AddonVersion fullAddonVersion = addonVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
        boolean isFullAddons = fullAddonVersion != null;

        if (!isFullAddons){
            ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
            if (fullContentVersion == null)
                throw new AS_15_8_POD_Exception("Ошибка при загрузке полного справочника аддонов. Не найдена запись загрузки полного ЕРДИ!");

            stateDetails = "Загрузка полного справочнка аддонов";
            log.info(stateDetails);
            addonRestClient.readFullFromNet(fullContentVersion.getRegUpdateTime());     // date - дата полной загрузки ЕРДИ
        }

        return !isFullAddons;
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

        log.info("---> Получение полного справочника ЕРДИ из ППП Анонимайзера: {}", uriComponents.toString());

        ResponseEntity<byte[]> entity = registryAnonimyzersRestTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );

        Files.write(path, entity.getBody());
        log.info("Полный справочник ЕРДИ сохранен в файл: {}", path.toString());
    }

    private void getAndSaveDeltaErdi(Path path, DeltaIdEntry deltaIdEntry) throws IOException, RestClientException {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaByDeltaId/{deltaId}/")
                        .build()
                        .expand(baseUrl, deltaIdEntry.deltaId);

        log.info("---> Получение дельты ЕРДИ id={} из ППП Анонимайзера: {}", deltaIdEntry.deltaId, uriComponents.toString());

        ResponseEntity<byte[]> entity = registryAnonimyzersRestTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );

        Files.write(path, entity.getBody());
        log.info("Дельта ЕРДИ с id={} сохранена в файл: {}", deltaIdEntry.deltaId, path.toString());
    }

    /**
     *
     * @return Результат - кол-во успешно загруженных дельт
     * @throws ExceptionErdiParser
     */
    public int loadAllDeltaERDI() {
        stateDetails = "Получение списка дельт ЕРДИ";

        int count = 0;
        DeltaIdEntry curDelta = null;
        try{
            List<DeltaIdEntry> list = getActualErdiDeltaEntries();
            count = list.size();

            log.info("Получен список дельт ЕРДИ. Размер = {}", count);
            log.info("Список дельт ЕРДИ: {}", list.stream().map(d -> d.deltaId).collect(Collectors.toList()).toString());

            if (count > 0){
                int i=0;
                for(DeltaIdEntry delta : list){
                    curDelta = delta;

                    stateDetails = String.format("Загрузка дельты ЕРДИ id=%d, %d/%d",
                            curDelta.deltaId, ++i, count);
                    log.info("--> " + stateDetails);
                    loadDeltaERDI(delta);
                }
            }
        }
        catch (Exception e){
            errorMessage = StringUtils.isEmpty(errorMessage) ?
                            (curDelta == null ?
                            "Ошибка при загрузке дельт ЕРДИ." :
                            "Ошибка при загрузке дельты ЕРДИ id=" + curDelta.deltaId)
                    : errorMessage;
            throw new AS_15_8_POD_Exception(errorMessage, e);
        }
        return count;
    }

    public int loadAllDeltaAddons() {
        stateDetails = "Получение списка дельт аддонов";

        DeltaAddonEntry curDelta = null;
        int count = 0;
        try{
            List<DeltaAddonEntry> list = addonRestClient.readDeltaList();
            count = list.size();

            log.info("Получен список дельт аддонов. Размер = {}", count);
            log.info("Список дельт аддонов: {}", list.stream().map(DeltaAddonEntry::getDeltaId).collect(Collectors.toList()).toString());

            if (count > 0) {
                int i=0;
                for (DeltaAddonEntry delta : list){
                    curDelta = delta;

                    stateDetails = String.format("Загрузка дельты аддона id=%d, %d/%d",
                            curDelta.getDeltaId(), ++i, count);
                    log.info("--> " + stateDetails);
                    addonRestClient.readDeltaFromNet(delta.getDeltaId(), delta.getActualDate());
                }
            }
        }
        catch (Exception e){
            errorMessage = StringUtils.isEmpty(errorMessage) ?
                    (curDelta == null ?
                            "Ошибка при загрузке дельт аддонов." :
                            "Ошибка при загрузке дельты аддона id=" + curDelta.getDeltaId())
                    : errorMessage;
            throw new AS_15_8_POD_Exception(errorMessage, e);
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

                    parser.parse(stream, (register, contents) -> {
                        allContents.addAll(contents);

                        String details = deltaIdEntry == null ?
                                "Разбор полного справочника ЕРДИ. ":
                                "Разбор дельты ЕРДИ id=" + deltaIdEntry.deltaId + ". ";
                        details += "Записей: " + allContents.size() + " ...";
                        log.info(details);

                        if (contents.size() == 0){
                            details = deltaIdEntry == null ?
                                    "Заливка полного справочника ЕРДИ в БД. ":
                                    "Заливка дельты ЕРДИ id=" + deltaIdEntry.deltaId + " в БД. ";
                            details += "Записей: " + allContents.size();
                            log.info(details);

                            try {
                                erdiLoaderService.fillContents(deltaIdEntry, register, allContents);
                            }
                            catch (ExceptionErdiLoad exceptionErdiLoad) {
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
                throw new AS_15_8_POD_Exception("Ошибка во время разбора файла ЕРДИ", ze);
            }
        }
        catch (Exception e) {
            throw new AS_15_8_POD_Exception("Ошибка во время разбора файла ЕРДИ", e);
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


