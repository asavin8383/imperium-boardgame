package services.checkUnitJob.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import services.checkUnitJob.CheckUnitJobService;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */
@Service
public class CheckUnitJobServiceImpl implements CheckUnitJobService {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public CheckUnitJobServiceImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids",
                arrangementJob.getErdiJobList().stream()
                .mapToLong(ERDIJob::getId)
                .boxed()
                .collect(Collectors.toList())
        );

        List<CheckUnitJob> checkUnitJobs = new ArrayList<>();
        try {
            checkUnitJobs = jdbcTemplate.query(
                "select content.id as id, 'DOMAIN' as check_unit_type, domain.domain as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.domain on content.id = domain.content_id and content.id IN (:ids)\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V4' as check_unit_type, ip.ip as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ip on content.id = ip.content_id and content.id IN (:ids)\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V4_SUBNET' as check_unit_type, ipsubnet.ipsubnet as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipsubnet on content.id = ipsubnet.content_id and content.id IN (:ids)\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V6' as check_unit_type, ipv6.ipv6 as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipv6 on content.id = ipv6.content_id and content.id IN (:ids)\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V6_SUBNET' as check_unit_type, ipv6subnet.\"ipv6Subnet\" as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipv6subnet on content.id = ipv6subnet.content_id and content.id IN (:ids)\n" +
                        "UNION\n" +
                        "select content.id, 'URL' as check_unit_type, url.url as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.url on content.id = url.content_id and content.id IN (:ids)",
                    parameters,
                    this::mapRowToCheckUnitJob
            );
            //TODO Некрасиво! Что-нибудь придумать
            for (CheckUnitJob checkUnitJob : checkUnitJobs){
                checkUnitJob.setArrangementID(arrangementJob.getId());
                checkUnitJob.setAccessToolUnit(arrangementJob.getAccessToolUnit());
            }

        } catch (Exception ex){

        }
        return checkUnitJobs;
    }

    private CheckUnitJob mapRowToCheckUnitJob(ResultSet rs, int rowNum) throws SQLException {
        CheckUnitJob checkUnitJob = new CheckUnitJob();
        checkUnitJob.setErdiID(rs.getLong("id"));
        CheckUnitType type = CheckUnitType.valueOf(rs.getString("check_unit_type"));
        checkUnitJob.setCheckUnit(new CheckUnit(type, rs.getString("check_unit_value")));
        return checkUnitJob;
    }
}
