package restapi.updaters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import restapi.SubTypeRestClient;

import java.util.Date;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:43
 */
@Component
@Slf4j
public class SubTypeDictionaryUpdater extends BaseDictionaryUpdater<SubTypeRestClient.SubTypeEntry>
{
    @Override
    protected String getTable() { return "sor.subtype"; }

    @Override
    protected String getId() { return "subtype_id"; }

    @Override
    protected void insertRecordInternal(SubTypeRestClient.SubTypeEntry record, Date now) {
        log.debug("inserting record {}", record);
        jdbcTemplate.update("insert into sor.subtype(ppn_dt, eff_dt, orig_id, c_date, registry_name, category_name, violation_name) values (?,?,?,?,?,?,?)",
                now, null,
                record.getId(), record.getDate(),
                record.getRegistry_name(), record.getCategory_name(), record.getViolation_name()
        );
   }
}
