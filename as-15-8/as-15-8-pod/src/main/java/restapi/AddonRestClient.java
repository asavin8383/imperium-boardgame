package restapi;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import exceptions.AS_15_8_POD_Exception;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.response.DeltaAddonEntry;
import model.response.DeltaIdEntry;
import model.scheme.AddonVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import repositories.AddonVersionRepository;
import updaters.AddonUpdater;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
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

    @Autowired
    RestClientUtils restClientUtils;

    @Autowired
    AddonVersionRepository addonVersionRepository;

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    private static final String ENTITY_NAME = "dumpAddons.xml";

    private XMLInputFactory xmlInputFactory;

    public AddonRestClient() {
        xmlInputFactory = XMLInputFactory.newFactory();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
    }

    @SneakyThrows
    public List<DeltaAddonEntry> readDeltaList() {
        List<DeltaIdEntry> erdiDeltaList = getActualAddonDeltaEntries();
        List<DeltaAddonEntry> addonDeltaList = erdiDeltaList.stream()
                .map(deltaIdEntry -> {
                    return new DeltaAddonEntry(
                            deltaIdEntry.deltaId,
                            deltaIdEntry.actualDate,
                            false);
                })
                .collect(Collectors.toList());

        log.info("Получен список дельт аддонов: {} элементов", addonDeltaList.size());
        return addonDeltaList;
    }

    public void readFullFromNet(Date regDate) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getFullERDIaddons/";
        processZIP(url, regDate, null);
    }

    public void readDeltaFromNet(long id, Date deltaDate) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getDumpDeltaAddonsByDeltaId/"+id+"/";
        processZIP(url, deltaDate, id);
    }

    /**
     * Находим во входном zip файл с именем {@value #ENTITY_NAME}
     * Читаем в потоковом режиме
     * Пишем в базу с заданным временем
     * @param url -- откуда читаем
     * @param date -- дата обновления
     * @param deltaId -- ID дельты. Если задана - читаем дельту, нет - полную выгрузку
     */
    private void processZIP(String url, Date date, Long deltaId) {
        log.info("GET from {}", url);
        ResponseEntity<byte[]> entity = restTemplate.getForEntity(url, byte[].class);

        byte[] resp = entity.getBody();

        if (restClientUtils.checkDeltaErdiNotFound(resp)){
            log.info("Аддон с ID = {} не найден! Корректная ситуация.", deltaId == null ? "<full>" : deltaId);
            return;
        }

        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(resp));
        try {
            ZipEntry entry = zipInputStream.getNextEntry();
            if(entry != null){
                log.debug("Zip entry name = {}", entry);
                if (ENTITY_NAME.equalsIgnoreCase(entry.getName())) {
                    XmlMapper mapper = new XmlMapper(xmlInputFactory);

                    XMLStreamReader sr = xmlInputFactory.createXMLStreamReader(zipInputStream);
                    sr.next(); // to point to <root>
                    sr.next(); // to point to root-element under root

                    int cnt = addonUpdater.insertAllJdbc(sr, mapper, date, deltaId);

                    log.info("{} addons processed", cnt);
                }
            } else {
                log.warn("По запросу к ППП получено содержимое без архивного файла: {}", url);
            }
        }
        catch (IOException | XMLStreamException e) {
            System.out.println(e);
            throw new AS_15_8_POD_Exception("Ошибка при разборе дельты аддона", e);
        }
    }

    private List<DeltaIdEntry> getActualAddonDeltaEntries() {
        Date dateUpdate = getActualAddonDate();
        if (dateUpdate == null){
            log.error("Ошибка! При получении списка дельт аддонов не была найдена запись с версией аддона");
            return new ArrayList<>();
        }

        log.info("Получение списка дельт ЕРДИ по дате {}", dateUpdate.toString());
        return restClientUtils.getDumpDeltaListByDate(dateUpdate);
    }

    private Date getActualAddonDate(){
        AddonVersion lastVersion = addonVersionRepository.findTopByIdNotNullOrderByIdDesc();
        Date actualDate = lastVersion == null ? null : (lastVersion.getRegUpdateTime() != null ?
                lastVersion.getRegUpdateTime() : lastVersion.getDeltaUpdateTime());
        return actualDate;
    }

}
