package schedule.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import schedule.QueryService;

/**
 * User: asinjavin
 * Date: 03.11.2019
 * Time: 12:55
 */
@Slf4j
@Service
public class QueryServiceImpl implements QueryService
{
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.sql.before-all:}")
    String sql_before_all;

    @Value("${app.sql.before-each:}")
    String sql_before_each;

    @Autowired
    public QueryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void beforeAll() {
        runQuery(sql_before_all);
    }

    @Override
    public void beforeEach(long rep_id) {
        runQuery(sql_before_each, rep_id);
    }

    private void runQuery(String sql, Object... params) {
        if (sql != null && !sql.isEmpty()) {
            log.debug("Running sql {}/{}", sql, params);
            jdbcTemplate.queryForObject(sql, params, Object.class);
            log.debug("sql completed");
        }
    }

}
