package common;

import exceptions.ExceptionErdiParser;
import lombok.extern.slf4j.Slf4j;
import model.rest.SubType;
import model.response.*;
import model.rest.control.PodInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import parsers.ErdiAddonsParser;
import parsers.ErdiFullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Component
@Slf4j
public class RestApiHelper {

    private RestTemplate restTemplate;

    private static final String urlRest = "";
    private static final String tempDir = "temp_dir";


    @Value("${spring.rest_base_url}")
    private String baseUrl;


    @Autowired
    public void restTemplateInit(RestTemplateBuilder restTemplateBuilder) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor("test158", "test158"));

        this.restTemplate = restTemplate;
    }


    public PodInfo getDateInfo(){
        ResponseEntity<RestResponseDumpDate> entity = restTemplate.exchange(
                baseUrl + "getLastDumpDate/",
                HttpMethod.GET, null, RestResponseDumpDate.class);

        System.out.println("**************");
        RestResponseDumpDate resp = entity.getBody();
        Long date = resp.getDumpLongDate();

        PodInfo podInfo = new PodInfo(new Date(date), new Date(date));
        return podInfo;
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
        List<PSEntry> list = resp.getListPSEntry();

        System.out.println(list.toString());
    }

    public void test3(){
        ResponseEntity<RestResponseDumpDate> entity = restTemplate.exchange(
                baseUrl + "getLastDumpDate/",
                HttpMethod.GET, null, RestResponseDumpDate.class);

        System.out.println("**************");
        RestResponseDumpDate resp = entity.getBody();
        Long date = resp.getDumpLongDate();

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
