package common;

import exceptions.ExceptionErdiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.ParamSor;
import model.rest.SubType;
import model.response.*;
import model.rest.control.PodState;
import model.scheme.AddonVersion;
import model.scheme.ContentVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import parsers.ErdiAddonsParser;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RestApiHelper {

    private static final String urlRest = "";
    private static final String tempDir = "temp_dir";

    private RestTemplate restTemplate;

    private final ParameterRepositoryExtend parameterRepository;
    private final ErdiLoaderService erdiLoaderService;
    private final ContentVersionRepository contentVersionRepository;
    private final AddonVersionRepository addonVersionRepository;;


    @Value("${spring.rest_base_url}")
    private String baseUrl;


    @Autowired
    public void restTemplateInit(RestTemplateBuilder restTemplateBuilder) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor("test158", "test158"));

        this.restTemplate = restTemplate;
    }


    public PodState getLoadState() throws ParseException {
        ResponseEntity<RestResponseDumpDate> entity = restTemplate.exchange(
                baseUrl + "/getLastDumpDate/",
                HttpMethod.GET, null, RestResponseDumpDate.class);

        RestResponseDumpDate resp = entity.getBody();
        Date date = (resp == null ? null : resp.getDumpDate());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String actualDumpDate = (date == null ? "" : dateFormat.format(date));
        String dateUpdate = parameterRepository.getParameterValue(ParamSor.ACTUAL_DATE.name());
        dateUpdate = dateUpdate == null ? "" : dateUpdate;

        String state = erdiLoaderService.getIsLoading() ? "PROCESS" : (erdiLoaderService.getIsError() ? "ERROR" : "");
        String errorMessage = !erdiLoaderService.getIsLoading() && erdiLoaderService.getIsError() ? erdiLoaderService.getMessageError() : "";

        ContentVersion lastContentVersion = contentVersionRepository.findTopByIdNotNullOrderByIdDesc();
        AddonVersion lastAddonVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();

        PodState podState = new PodState(dateUpdate, actualDumpDate, state, errorMessage,
                lastContentVersion == null ? null : lastContentVersion.getId(),
                lastAddonVersion == null ? null : lastAddonVersion.getId());
        return podState;
    }


    public void startUpdateErdi(){
        String strIsFullErdi = parameterRepository.getParameterValue(ParamSor.IS_FULL_ERDI_LOADED.name());
        boolean isFullErdi = !StringUtils.isEmpty(strIsFullErdi) && strIsFullErdi.equals("1");

        System.out.println("startUpdateErdi --> fullErdi = " + isFullErdi);

        if (!isFullErdi){
            System.out.println("Start load full erdi");
        }
        else {
            System.out.println("Start load delta erdi");
        }


        try {
            System.out.println("*****************");
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("+++++++++++");
            });
            thread.start();
            System.out.println("*****************");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    public void test4(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateFormat.format(date);

        System.out.println(dateStr);

        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaListByDate/{dateStr}/")
                        .build()
                        .expand(baseUrl, dateStr);

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
