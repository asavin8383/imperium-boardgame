package restapi;


import exceptions.ExceptionErdiLoad;
import exceptions.ExceptionErdiParser;
import lombok.extern.slf4j.Slf4j;
import model.rest.ContentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import parsers.ErdiFullParser;
import services.ErdiLoaderService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
@Slf4j
public class ErdiRestClient {

    private RestTemplate restTemplate;

    @Autowired
    ErdiLoaderService erdiLoaderService;

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


    public void removeLastContentVersion() {
        erdiLoaderService.removeLastContentVersion();
    }


    public void fillFullErdiToDB() throws IOException, ExceptionErdiParser {
        //erdiLoaderService.clearAllScheme();

        //erdiLoaderService.removeLastContentVersion();

        //readFullErdi(Paths.get(tempDir, "mytest.zip"));
        //readFullErdi(Paths.get(tempDir, "mytest2.zip"));
        //readFullErdi(Paths.get(tempDir, "mytest3_delete.zip"));
        readFullErdi(Paths.get(tempDir, "full_erdi_2019-10-02__02_10_44.zip"));


        if (true)
            return;


        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd__hh_mm_ss");
        //Path path = Paths.get(tempDir, String.format("full_erdi_%s.zip", dateFormat.format(new Date())));
        Path path = Paths.get(tempDir, "full_erdi_2019-10-02__02_10_44.zip");

        try{
            //Files.deleteIfExists(path);
            System.out.println("----> getting full ERDI: " + path.toString());
            //getAndSaveFullErdi(path);
        }
        catch (Exception e){
            try{
                Files.deleteIfExists(path);
            }
            catch (IOException ie){}
            throw e;
        }

        try{
            System.out.println("----> saving full ERDI: " + path.toString());
            readFullErdi(path);
            //Files.deleteIfExists(path);
        }
        catch (Exception e){
            /*try{
                Files.deleteIfExists(path);
            }
            catch (IOException ie){}*/
            throw e;
        }

        System.out.println("+++++++++++++++++");

        //System.out.println(date);
    }

    public void getAndSaveFullErdi(Path path) throws IOException, RestClientException {

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

        File directory = path.toFile().getParentFile();
        if (!directory.exists()){
            directory.mkdirs();
        }

        Files.write(path, entity.getBody());
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

                List<ContentRest> allContents = new ArrayList<>();
                log.info("start parsing... ");

                parser.parse(stream, (register, contents) -> {
                    allContents.addAll(contents);
                    log.info("parsing... " + allContents.size());

                    if (contents.size() == 0){
                        try {
                            erdiLoaderService.fillContents(null, register, allContents);
                        } catch (ExceptionErdiLoad exceptionErdiLoad) {
                            exceptionErdiLoad.printStackTrace();
                        }
                        return false;
                    }
                    return true;
                });
            }
        }
    }


}


