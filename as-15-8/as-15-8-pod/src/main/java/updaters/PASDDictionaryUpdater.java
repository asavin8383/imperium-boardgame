package updaters;

import lombok.extern.slf4j.Slf4j;
import model.response.PASDEntry;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:43
 */
@Component
@Slf4j
public class PASDDictionaryUpdater extends BaseDictionaryUpdater<PASDEntry>
{
    @Override
    protected String getTable() { return "sor.pasd"; }

    @Override
    protected String getId() { return "pasd_id"; }

    @Override
    protected void insertRecordInternal(PASDEntry record, Date now) {
        log.debug("inserting record {}", record);
        jdbcTemplate.update("insert into sor.pasd(ppn_dt, eff_dt, orig_id, c_date, name, hostname, domainnames, servicedescription, networkaddresses, ipaccessfgis, credentials) values (?,?,?,?,?,?,?,?,?,?,?)",
                now, null,
                record.getId(), record.getDate(),
                record.getName(), record.getHostname(), record.getDomainNames(), record.getServiceDescription(), record.getNetworkAddresses(), record.getIpAccessFgis(), record.getCredentials()
        );
   }
}
