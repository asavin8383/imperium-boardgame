package restapi;


import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.ExceptionErdiLoad;
import exceptions.ExceptionErdiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.ParamSor;
import model.response.DeltaIdEntry;
import model.response.RestResponseDeltaErdi;
import model.response.RestResponseDeltaListByDate;
import model.rest.ContentRest;
import model.scheme.ContentVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import parsers.ErdiFullParser;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
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


    private static final String urlRest = "";
    private static final String tempDir = "temp_dir";

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    private boolean isError = false;
    private boolean isLoading = false;
    private String errorMessage = "";
    private String stateDetails = "";


    public void removeLastContentVersion() {
        if (isLoading){
            return;
        }
        isLoading = true;

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
            isLoading = false;
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

    public void loadFullERDI() throws IOException, ExceptionErdiParser {
        if (isLoading){
            return;
        }
        isLoading = true;
        isError = false;
        errorMessage = "";

        boolean deleteTempFile = false;

        /*
        //erdiLoaderService.clearAllScheme();

        //erdiLoaderService.removeLastContentVersion();

        //erdiToDB(Paths.get(tempDir, "mytest.zip"));
        //erdiToDB(Paths.get(tempDir, "mytest2.zip"));
        //erdiToDB(Paths.get(tempDir, "mytest3_delete.zip"));
        erdiToDB(Paths.get(tempDir, "full_erdi_2019-10-02__02_10_44.zip"), null);

        if (true)
            return;
        */

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd__hh_mm_ss");
        Path path = Paths.get(tempDir, String.format("full_erdi_%s.zip", dateFormat.format(new Date())));

        try{
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
            isLoading = false;
            stateDetails = isError ? errorMessage : "Загрузка полного справочника ЕРДИ прошла успешно";
            if (deleteTempFile){
                try{
                    Files.deleteIfExists(path);
                }
                catch (IOException ie){}
            }
        }
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
        if (isLoading){
            return;
        }
        isLoading = true;
        isError = false;
        errorMessage = "";

        int count = 0;
        try{
            List<DeltaIdEntry> deltaList = getLastDumpDeltaListByDate();
            count = deltaList.size();

            log.info("Получен список дельт: размер=" + count);
            log.info(deltaList.toString());

            if (count > 0){
                for(DeltaIdEntry deltaIdEntry : deltaList){
                    loadDeltaERDI(deltaIdEntry);
                    if (isError)
                        throw new ExceptionErdiParser("Error load delta");
                }
            }
        }
        catch (Exception e){
            errorMessage = "Ошибка загрузки дельта ЕРДИ";
            isError = true;
            throw new ExceptionErdiParser(e);
        }
        finally {
            isLoading = false;
            stateDetails = isError ? errorMessage : "Загрузка дельт (" + count + ") ЕРДИ прошла успешно";
        }
    }


    public List<DeltaIdEntry> getLastDumpDeltaListByDate() throws ParseException {
        String dateUpdate = parameterRepository.getParameterValue(ParamSor.ACTUAL_DATE.name());
        //dateUpdate = "2017-09-22T09:23:13";

        System.out.println("dateUpdate = " + dateUpdate);
        if (StringUtils.isEmpty(dateUpdate)){
            return new ArrayList<>();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = dateFormat.parse(dateUpdate);
        return getDumpDeltaListByDate(date);
    }

    @Transactional
    public String setParameter(String name, String value) {
        if (isLoading){
            return "НЕ удалось обновить параметр! Процесс занят: " + stateDetails;
        }

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
        String dateStr = dateFormat.format(date);

        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaListByDate/{dateStr}/")
                        .build()
                        .expand(baseUrl, dateStr);

        ResponseEntity<RestResponseDeltaListByDate> entity;
        try {
            entity = registryAnonimyzersRestTemplate.exchange(
                    uriComponents.toString(),
                    HttpMethod.GET,
                    null,
                    RestResponseDeltaListByDate.class
            );
        }
        catch (Throwable e){
            System.out.println("Ошибка загрузки списка дельт по дате: " + dateStr);
            throw e;
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
        log.info("Попытка парсинга {0}, file: {1}", deltaIdEntry != null ? "Delta ERDI" : "Full ERDI", path.toAbsolutePath().toString());

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
                    log.info("Начат парсинг записи " + entryName + "...");
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
            RestResponseDeltaErdi restResponse = mapper.readValue(content, RestResponseDeltaErdi.class);
            return !StringUtils.isEmpty(restResponse.response) && restResponse.response.equalsIgnoreCase("Not found");
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}


