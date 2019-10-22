package restapi.updaters;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import model.response.AddonEntry;
import model.scheme.AddonVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repositories.AddonVersionRepository;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * User: asinjavin
 * Date: 19.10.2019
 * Time: 1:35
 */
@Service
@Slf4j
public class AddonUpdater
{
    @Autowired
    AddonVersionRepository addonVersionRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRED)
    public int insertAllJdbc(XMLStreamReader sr, XmlMapper mapper, Date date, boolean is_delta) throws XMLStreamException, IOException {

        // Кешируем текущие content_id и content_version_id для существующих ЕРДИ
        Map<Long, Long> erdiToContentId = new HashMap<>();
        Map<Long, Long> erdiToContentVersionId = new HashMap<>();
        jdbcTemplate.query("select content_id, erdi_id, content_version_id " +
                "from sor.content c inner join sor.content_history ch on c.id = ch.content_id and ch.end_dt='3000-01-01'", resultSet -> {
            Long erdi_id = resultSet.getLong("erdi_id");

            Long content_id = resultSet.getLong("content_id");
            erdiToContentId.put(erdi_id, content_id);

            Long content_version_id = resultSet.getLong("content_version_id");
            erdiToContentVersionId.put(erdi_id, content_version_id);
        });

        // Собираем массив аддонов для вставки
        int cnt = 0;
        int nf = 0;
        List<AddonEntry> addonEntries = new ArrayList<>();
        while (sr.hasNext()) {

            cnt++;
            try {
                AddonEntry addonEntry = mapper.readValue(sr, AddonEntry.class);

                long erdi_id = addonEntry.getId();
                if (!erdiToContentId.containsKey(erdi_id)) {
                    nf++;
                    log.warn("{}: ERDI {} was not found! ", cnt, erdi_id );
                    continue;
                }
                addonEntries.add(addonEntry);

                if (reachesTheEnd(sr)) break;

            } catch (java.util.NoSuchElementException e) {
                break;
            }
        }

        log.info("{} ERDIs was not found", nf);


        AddonVersion addonVersion = new AddonVersion();
        addonVersion.setPpnDate(new Date());
        if (is_delta) addonVersion.setDeltaUpdateTime(new Date());
        else addonVersion.setRegUpdateTime(new Date());
        addonVersionRepository.save(addonVersion);
        addonVersionRepository.flush();

        Long addonVersionId = addonVersion.getId();
        java.sql.Date now = new java.sql.Date(date.getTime());

        log.info("Inserting {} addons with version {}", addonEntries.size(), addonVersionId);

        jdbcTemplate.batchUpdate("insert into sor.addon(addon_version_id, orig_id, info_type_id, visitors_cnt_russia, visitors_cnt_world) VALUES (?,?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement pst, int i) throws SQLException {
                        AddonEntry addonEntry = addonEntries.get(i);
                        pst.setLong(1, addonVersionId);
                        pst.setLong(2, addonEntry.getId());
                        pst.setString(3, addonEntry.getInfoTypeId());
                        pst.setObject(4, addonEntry.getVisitorsCntRussia());
                        pst.setObject(5, addonEntry.getVisitorsCntWorld());
                    }

                    @Override
                    public int getBatchSize() {
                        return addonEntries.size();
                    }
        });

        log.info("Inserting {} content_histories with version {}", addonEntries.size(), addonVersionId);
        jdbcTemplate.batchUpdate("insert into sor.content_history(content_id, content_version_id, addon_version_id, st_dt) values (?,?,?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement pst, int i) throws SQLException {
                        AddonEntry addonEntry = addonEntries.get(i);
                        pst.setLong(1, erdiToContentId.get(addonEntry.getId()));
                        pst.setLong(2, erdiToContentVersionId.get(addonEntry.getId()));
                        pst.setLong(3, addonVersionId);
                        pst.setDate(4, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return addonEntries.size();
                    }
        });

        return cnt;
    }

    private boolean reachesTheEnd(XMLStreamReader sr) throws XMLStreamException {
        int r;
        //noinspection StatementWithEmptyBody
        for (r = sr.next(); r == XMLStreamConstants.CHARACTERS; r = sr.next()){}
        return r == XMLStreamConstants.END_ELEMENT;
    }


}
