package restapi.updaters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:08
 */
@Component
@Slf4j
public abstract class BaseDictionaryUpdater<T>
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    protected abstract String getTable();

    protected abstract String getId();

    /**
     * Метод лля добавления одной записи
     * @param record
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRecord(T record) {
       // Дата обновления, будет одинакова во всех записях
        Date now = new Date();
        insertRecordInternal(record, now);
        log.debug("1 record inserted");
        updateTable(now);
    }

    /**
     * Метод для добавления нескольких записей, например, из rest
     * @param records
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRecords(Collection<T> records) {
       // Дата обновления, будет одинакова во всех записях
        Date now = new Date();
        for (T record: records)
            insertRecordInternal(record, now);
        log.debug("{} records inserted", records.size());
        updateTable(now);
    }

    /**
     * Метод для вставки записи в базу
     * Возможно, заменю на JPA
     * @param record
     * @param now
     */
    @Transactional(propagation = Propagation.MANDATORY)
    protected abstract void insertRecordInternal(T record, Date now);

    /**
     * Актуализация старых и новых записей.
     * Старые записи имеют eff_dt = '3000-01-01'
     * Новые записи имеют eff_dt is null
     * Сравнение записей проиисходит по полю orig_id, в котором хранится ID из источника данных и по c_date, в котором должна быть Date ищ источника.
     * Мы считаем, что ID записи бадет оставаться постоянным, а Date будет меняться при обновлении записи.
     * Обновление происхидит в 4 этапа.
     * 1. Старые записи, для которых нет новых записей с тем же ID, считаются удаленными и помечаются как исторические (eff_dt = '3000-01-01')
     * 2. Старые записи, для которых есть новые записи с тем же ID, но большей Date, помечаются как исторические (eff_dt = '3000-01-01')
     * 3. Новые записи, для которых есть старые записи с теми же ID и Date, считаются не изменившимися, и удаляются.
     *    Тут же удаляются новые записи с Date, меньше существующей, если такие вдруг придут.
     * 4. Остальные новые записи помечаются как актуальные. Туда попадают записи из шага 2 и совсем новые, для которых нет записей с таким же Id
     *
     * @param now
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateTable(Date now) {
        String ID=getId();
        String TABLE= getTable();

        String sql;
        int rc;

        //-- Старые записи, для которых нет новых записей
        //-- Отправить в историю
        sql = "update " + TABLE + " set eff_dt = ? where " + ID + " in (" +
                "select " + ID + " from " + TABLE + " old where eff_dt = '3000-01-01' and not exists(" +
                "  select * from " + TABLE + " new where new.eff_dt is null " +
                "  and new.orig_id = old.orig_id" +
                ") )";
        log.debug(sql);
        rc = jdbcTemplate.update(sql, now);
        log.debug("{} removed records archived", rc);

        //-- Старые записи, для которых есть новые записи, у которых c_date больше
        //-- Отправить в историю
        sql = "update " + TABLE + " set eff_dt = ? where " + ID + " in (" +
                "select " + ID +" from " + TABLE +" old where eff_dt = '3000-01-01' and exists(" +
                "  select * from " + TABLE +" new where new.eff_dt is null " +
                "  and new.orig_id = old.orig_id" +
                "  and new.c_date > old.c_date" +
                ") )";
        log.debug(sql);
        rc = jdbcTemplate.update(sql, now);
        log.debug("{} old records archived", rc);

        //-- Новые записи, для которых есть старые записи, с совпадающими orig_id и c_date равной или меньше сушествующей(!)
        //-- Удалить
        sql ="delete from " + TABLE +" where " + ID +" in (" +
                "select " + ID +" from " + TABLE +" new where eff_dt is null and exists(" +
                "  select * from " + TABLE +" old where old.eff_dt = '3000-01-01' " +
                "  and new.orig_id = old.orig_id" +
                "  and new.c_date <= old.c_date" +
                ") )";
        log.debug(sql);
        rc = jdbcTemplate.update(sql);
        log.debug("{} new redundant records removed", rc);

        // Все остальные новые записи помечаем как актуальные
        sql = "update " + TABLE + " set eff_dt = '3000-01-01' where eff_dt is null ";
        log.debug(sql);
        rc = jdbcTemplate.update(sql);
        log.debug("{} new records actualized", rc);

    }
}
