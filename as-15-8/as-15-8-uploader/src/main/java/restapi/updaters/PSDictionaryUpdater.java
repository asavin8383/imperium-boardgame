package restapi.updaters;

import lombok.extern.slf4j.Slf4j;
import model.response.PSEntry;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:43
 */
@Component
@Slf4j
public class PSDictionaryUpdater extends BaseDictionaryUpdater<PSEntry>
{
    @Override
    protected String getTable() { return "sor.ps"; }

    @Override
    protected String getId() { return "ps_id"; }

    @Override
    protected void insertRecordInternal(PSEntry record, Date now) {
        log.debug("inserting record {}", record);
        jdbcTemplate.update("insert into sor.ps(ppn_dt, eff_dt, orig_id, c_date, name, hostname) values (?,?,?,?,?,?)",
                now, null, record.getId(), record.getDate(), record.getName(), record.getHostname()
        );
   }
}
