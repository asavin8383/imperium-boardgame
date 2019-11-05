package restapi;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.response.DeltaAddonEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import updaters.AddonUpdater;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:43
 */
@Component
@Slf4j
public class AddonRestClient
{
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    AddonUpdater addonUpdater;

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    private static final String ENTITY_NAME= "dumpAddons.xml";

    private XMLInputFactory xmlInputFactory;

    public AddonRestClient() {
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    }

    @SneakyThrows
    public List<DeltaAddonEntry> readDeltaList() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        List<DeltaAddonEntry> res = new ArrayList<>();
        res.add(new DeltaAddonEntry(42, dateFormat.parse("2017-09-22T10:27:04"), false));
        return res;
    }

    public void readFullFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getFullERDIaddons/";
        processZIP(url, new Date(), false);

    }

    public void readDeltaFromNet(long id, Date date) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getDumpDeltaAddonsByDeltaId/"+id+"/";
        processZIP(url, date, true);
    }

    /**
     * Находим во входном zip файл с именем {@value #ENTITY_NAME}
     * Читаем в потоковом режиме
     * Пишем в базу с заданным временем
     * @param url -- откуда читаем
     * @param date -- дата обновления
     * @param is_delta -- читаем дельту или полную выгрузку
     */
    private void processZIP(String url, Date date, boolean is_delta) {
        log.info("GET from {}", url);
        ResponseEntity<byte[]> entity = restTemplate.getForEntity(url, byte[].class);

        byte[] resp = entity.getBody();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(resp));
        try {
            ZipEntry entry = zipInputStream.getNextEntry();
            log.debug("Zip entry name = {}", entry);
            if (ENTITY_NAME.equalsIgnoreCase(entry.getName())) {
                XmlMapper mapper = new XmlMapper(xmlInputFactory);

                XMLStreamReader sr = xmlInputFactory.createXMLStreamReader(zipInputStream);
                sr.next(); // to point to <root>
                sr.next(); // to point to root-element under root

                int cnt = addonUpdater.insertAllJdbc(sr, mapper, date, is_delta);

                log.info("{} addons processed", cnt);

            }
        } catch (IOException | XMLStreamException e) {
            System.out.println(e);
        }
    }

}
