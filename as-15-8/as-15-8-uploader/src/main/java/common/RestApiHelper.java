package common;

import exceptions.ExceptionErdiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.response.*;
import model.rest.SubType;
import model.rest.control.PodState;
import model.scheme.AddonVersion;
import model.scheme.ContentVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import parsers.ErdiAddonsParser;
import parsers.ErdiFullParser;
import repositories.AddonVersionRepository;
import repositories.ContentVersionRepository;
import repositories.impl.ParameterRepositoryExtend;
import restapi.ErdiRestClient;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RestApiHelper {

    private static final String urlRest = "";
    private static final String tempDir = "temp_dir";

    private final RestTemplate restTemplate;

    private final ParameterRepositoryExtend parameterRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final AddonVersionRepository addonVersionRepository;
    private final ErdiRestClient erdiRestClient;


    @Value("${spring.rest_base_url}")
    private String baseUrl;

    public PodState getLoadState() throws ParseException {
        ResponseEntity<RestResponseDumpDate> entity = restTemplate.exchange(
                baseUrl + "/getLastDumpDate/",
                HttpMethod.GET, null, RestResponseDumpDate.class);

        RestResponseDumpDate resp = entity.getBody();
        Date dateDumpDate = (resp == null ? null : resp.getDumpDate());
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        AddonVersion lastAddonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();

        Date dateUpdate = erdiRestClient.getActualContentDate();
        String strDateUpdate = dateUpdate == null ? "" : dateFormat1.format(dateUpdate);
        String actualServerDumpDate = (dateDumpDate == null ? "" : dateFormat1.format(dateDumpDate));

        String state = erdiRestClient.getState();
        String errorMessage = erdiRestClient.getErrorMessage();
        String stateDetails = erdiRestClient.getStateDetails();

        List<ContentVersion> allContents = contentVersionRepository.findAll(Sort.by(Sort.Order.asc("id")));
        List<Long> allContentsInt = allContents.stream().map(contentVersion -> contentVersion.getId()).collect(Collectors.toList());

        PodState podState = new PodState(strDateUpdate, actualServerDumpDate, state, errorMessage, stateDetails,
                lastContentVersion == null ? null : lastContentVersion.getId(),
                lastAddonVersion == null ? null : lastAddonVersion.getId(),
                allContentsInt);
        return podState;
    }


    public void startUpdateErdi(){
        ContentVersion fullContentVersion = contentVersionRepository.getTopByRegUpdateTimeNotNullOrderByIdDesc();
        boolean isFullErdi = fullContentVersion != null;

        log.info("---> Запуск зугрузки ЕРДИ: " + (isFullErdi ? "дельт" : "полного справочника"));

        CompletableFuture
                .runAsync(() -> {
                    try{
                        if (!isFullErdi) {
                            erdiRestClient.loadFullERDI();
                        }
                        else {
                            erdiRestClient.loadAllDeltaERDI();
                        }
                    }
                    catch(Exception ex){
                        throw new RuntimeException(ex);
                    }
                }).exceptionally(ex -> {
            log.info("Ошибка при загрузке ЕРДИ", ex);
            throw new CompletionException(ex);
        });
    }

    public void removeLastContentVersion(){
        erdiRestClient.removeLastContentVersion();
    }

    public void removeVersionTo(int version){
        CompletableFuture
                .runAsync(() -> {
                    while(true){
                        ContentVersion last = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
                        if (last == null || version >= last.getId())
                            return;
                        erdiRestClient.removeLastContentVersion();
                    }
                });
    }

    public String setParameter(String name, String value){
        return erdiRestClient.setParameter(name, value);
    }

    public void test1(){
        ResponseEntity<RestResponsePS> entity = restTemplate.exchange(
                baseUrl + "getPSList/",
                HttpMethod.GET, null, RestResponsePS.class);

        System.out.println("**************");
        RestResponsePS resp = entity.getBody();
        List<PSEntry> list = resp.getListPSEntry();

        System.out.println(list.toString());
    }

    public void test2(){
        ResponseEntity<RestResponsePASD> entity = restTemplate.exchange(
                baseUrl + "getPASDList/",
                HttpMethod.GET, null, RestResponsePASD.class);

        System.out.println("**************");
        RestResponsePASD resp = entity.getBody();
        List<PASDEntry> list = resp.getListPSEntry();

        System.out.println(list.toString());
    }

    public void test3() throws ParseException {
        ResponseEntity<RestResponseDumpDate> entity = restTemplate.exchange(
                baseUrl + "getLastDumpDate/",
                HttpMethod.GET, null, RestResponseDumpDate.class);

        System.out.println("**************");
        RestResponseDumpDate resp = entity.getBody();
        Date date = resp.getDumpDate();

        System.out.println(date);
    }



    public void test5(int id) {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaListByDeltaId/{id}/")
                        .build()
                        .expand(baseUrl, id);

        System.out.println(uriComponents.toString());

        ResponseEntity<RestResponseDeltaListByDate> entity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                RestResponseDeltaListByDate.class
        );

        System.out.println("**************");
        System.out.println(entity.getBody());

        RestResponseDeltaListByDate resp = entity.getBody();
        List<DeltaIdEntry> list = resp.response;
        System.out.println("list = " + list.size());

        //System.out.println(date);
    }


    public void test88() {
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getSubTypeList/")
                        .build()
                        .expand(baseUrl);

        System.out.println(uriComponents.toString());

        ResponseEntity<RestResponseSubTypeList> entity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                RestResponseSubTypeList.class
        );

        System.out.println("**************");
        System.out.println(entity.getBody());

        RestResponseSubTypeList resp = entity.getBody();
        List<SubType> list = resp.subTypeList();
        System.out.println(list.toString());


        //System.out.println(date);
    }

    public void test6() throws IOException, ExceptionErdiParser {

        Path path = Paths.get(tempDir, "mytest.zip");

        //writeFullErdi();

        readFullErdi(path);

        System.out.println("+++++++++++++++++");

        //System.out.println(date);
    }

    public void readFullErdi(Path path) throws IOException, ExceptionErdiParser {
        System.out.println(path.toAbsolutePath().toString());

        ZipFile zipFile = new ZipFile(path.toFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            System.out.println("Entry name = " + entry.getName());
            if (entry.getName().equals("dump.xml")){
                InputStream stream = zipFile.getInputStream(entry);

                ErdiFullParser parser = new ErdiFullParser();
                parser.parse(stream, (register, contents) -> {
                    System.out.println("register: " + register.toString());
                    System.out.println("contents: " + contents.toString());
                    return true;
                });
            }
        }
    }

    public void writeFullErdi(Path path) throws IOException {

        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getFullERDI/")
                        .build()
                        .expand(baseUrl);

        System.out.println(uriComponents.toString());

        ResponseEntity<byte[]> entity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );

        System.out.println("**************");
        System.out.println(entity.getBody().length);

        File directory = path.toFile().getParentFile();
        if (!directory.exists()){
            directory.mkdirs();
        }

        Files.deleteIfExists(path);
        Files.write(path, entity.getBody());
    }

    public void test7() throws IOException, ExceptionErdiParser {

        Path path = Paths.get(tempDir, "addons.zip");
        System.out.println(path.toAbsolutePath().toString());

        //writeAddonsErdi(path);
        readAddonsErdi(path);

        System.out.println("+++++++++++++++++");

        //System.out.println(date);
    }

    public void readAddonsErdi(Path path) throws IOException, ExceptionErdiParser {
        ZipFile zipFile = new ZipFile(path.toFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals("dumpAddons.xml")){
                InputStream stream = zipFile.getInputStream(entry);
                ErdiAddonsParser parser = new ErdiAddonsParser(3);
                parser.parse(stream, (register, contentAddons) -> {
                    System.out.println("contentAddons: " + contentAddons.toString());
                    return true;
                });
            }
        }
    }

    public void writeAddonsErdi(Path path) throws IOException {

        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getFullERDIaddons/")
                        .build()
                        .expand(baseUrl);

        System.out.println(uriComponents.toString());

        ResponseEntity<byte[]> entity = restTemplate.exchange(
                uriComponents.toString(),
                HttpMethod.GET,
                null,
                byte[].class
        );

        System.out.println("**************");
        System.out.println(entity.getBody().length);

        File directory = path.toFile().getParentFile();
        if (!directory.exists()){
            directory.mkdirs();
        }

        Files.deleteIfExists(path);
        Files.write(path, entity.getBody());
    }


}
