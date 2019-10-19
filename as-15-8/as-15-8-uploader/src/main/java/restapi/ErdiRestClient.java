package restapi;


import exceptions.ExceptionErdiLoad;
import exceptions.ExceptionErdiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.response.DeltaIdEntry;
import model.rest.ContentRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErdiRestClient {

    private final RestTemplate registryAnonimyzersRestTemplate;
    private final ErdiLoaderService erdiLoaderService;

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
        try {
            stateDetails = "Удаление последней контент версии";
            erdiLoaderService.removeLastContentVersion();
        }
        catch (Exception e){
            isError = true;
            errorMessage = "Удаление последней версии завершилось с ошибкой";
        }
        finally {
            stateDetails = "";
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
            stateDetails = "";
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

    private void erdiToDB(Path path, DeltaIdEntry deltaIdEntry) throws IOException, ExceptionErdiParser {
        System.out.printf("Try parsing %s, file: %s \n", deltaIdEntry != null ? "Delta ERDI" : "Full ERDI", path.toAbsolutePath().toString());

        ZipFile zipFile = new ZipFile(path.toFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();

            String entryName = (deltaIdEntry != null ? "dump_delta.xml" : "dump.xml");

            if (entry.getName().equals(entryName)){
                InputStream stream = zipFile.getInputStream(entry);
                ErdiFullParser parser = new ErdiFullParser();

                List<ContentRest> allContents = new ArrayList<>();
                log.info("start parsing entry " + entryName + "...");
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


}


