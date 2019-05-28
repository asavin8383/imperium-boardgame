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
import enums.AccessToolUnit;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import model.ArrangementResult;
import repositories.ArrangementResultRepository;
import services.checkUnitJob.CheckUnitJobService;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */
@Service
public class CheckUnitJobServiceImpl implements CheckUnitJobService {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private ArrangementResultRepository arrangementResultRepo;

    @Autowired
    public CheckUnitJobServiceImpl(NamedParameterJdbcTemplate jdbcTemplate,
                                   ArrangementResultRepository arrangementResultRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.arrangementResultRepo = arrangementResultRepo;
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

        CheckUnitJobMapper mapper = new CheckUnitJobMapper(arrangementJob.getAccessToolUnit());
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
                    (result, rowNum) -> mapper.map(result, rowNum)
            );

        } catch (Exception ex){

        }
        return checkUnitJobs;
    }

    @Override
    public Long saveCheckUnitJobAsResult(CheckUnitJob checkUnitJob) {
        try{
            ArrangementResult arrangementResult = new ArrangementResult();
            arrangementResult.setCheckUnitType(checkUnitJob.getCheckUnit().getType());
            arrangementResult.setCheckUnitValue(checkUnitJob.getCheckUnit().getValue());
            return arrangementResultRepo.save(arrangementResult).getId();
        }catch (Exception ex){
            throw new AS_15_8_DispatcherException("Error saving arrangement result by check unit job!", ex);
        }
    }
    
    class CheckUnitJobMapper{
    	
    	private AccessToolUnit accessToolUnit;
    	
    	public CheckUnitJobMapper(AccessToolUnit accessToolUnit) {
			this.accessToolUnit = accessToolUnit;
		}
    	
        public CheckUnitJob map(ResultSet rs, int rowNum) throws SQLException {
            CheckUnitJob checkUnitJob = new CheckUnitJob();
            checkUnitJob.setAccessToolUnit(accessToolUnit);
            CheckUnitType type = CheckUnitType.valueOf(rs.getString("check_unit_type"));
            checkUnitJob.setCheckUnit(new CheckUnit(type, rs.getString("check_unit_value")));
            return checkUnitJob;
        }
    }
}
