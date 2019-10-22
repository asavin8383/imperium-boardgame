package restapi;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import restapi.updaters.AddonUpdater;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    private XMLInputFactory xmlInputFactory;

    public AddonRestClient() {
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    }

    /**
     * Находим во входном zip файл с именем dumpAddons.xml
     * Читаем в потоковом режиме
     * Пишем в базу с текущим временем
     */
    public void readFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getFullERDIaddons/";
        log.info("GET from {}", url);
        ResponseEntity<byte[]> entity = restTemplate.getForEntity(url, byte[].class);

        byte[] resp = entity.getBody();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(resp));
        try {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry.getName().equalsIgnoreCase("dumpAddons.xml")) {
                XmlMapper mapper = new XmlMapper(xmlInputFactory);

                XMLStreamReader sr = xmlInputFactory.createXMLStreamReader(zipInputStream);
                sr.next(); // to point to <root>
                sr.next(); // to point to root-element under root

                int cnt = addonUpdater.insertAllJdbc(sr, mapper);

                log.info("{} addons processed", cnt);

            }
        } catch (IOException | XMLStreamException e) {
            System.out.println(e);
        }

    }

}
