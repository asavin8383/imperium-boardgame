package services.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import enums.AccessToolParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolUnit;
import enums.ArrangementStatus;
import enums.CheckUnitJobResult;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import model.ArrangementResult;
import repositories.ArrangementResultRepository;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.CheckUnitJobService;

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

        CheckUnitJobMapper mapper = new CheckUnitJobMapper(arrangementJob.getAccessToolUnit(), arrangementJob.getAccessToolParameters());
        List<CheckUnitJob> checkUnitJobs = new ArrayList<>();
        try {
            checkUnitJobs = jdbcTemplate.query(
                "select content.id as id, 'DOMAIN' as check_unit_type, domain.domain as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.domain on content.id = domain.content_id and content.id IN (:ids) and blocktype in ('domain', 'domain-mask')\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V4' as check_unit_type, ip.ip as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ip on content.id = ip.content_id and content.id IN (:ids) and blocktype = 'ip'\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V4_SUBNET' as check_unit_type, ipsubnet.ipsubnet as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipsubnet on content.id = ipsubnet.content_id and content.id IN (:ids) and blocktype = 'ip'\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V6' as check_unit_type, ipv6.ipv6 as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipv6 on content.id = ipv6.content_id and content.id IN (:ids) and blocktype = 'ip'\n" +
                        "UNION\n" +
                        "select content.id, 'IP_V6_SUBNET' as check_unit_type, ipv6subnet.\"ipv6Subnet\" as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.ipv6subnet on content.id = ipv6subnet.content_id and content.id IN (:ids) and blocktype = 'ip'\n" +
                        "UNION\n" +
                        "select content.id, 'URL' as check_unit_type, url.url as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.url on content.id = url.content_id and content.id IN (:ids) and blocktype is null",
                    parameters,
                    (rs, rowNum) -> {
                    	CheckUnitJob job = mapper.map(rs, rowNum);
                    	Long jobID = saveCheckUnitJobAsResult(arrangementJob.getId(), rs.getLong("id"), job);
                    	job.setJobID(jobID);
                    	return job;
                    }
            );

        } catch (Exception ex){
        	throw new AS_15_8_DispatcherException("Ошибка при создании заданий на проверку запрещенных ресурсов!", ex);
        }
        return checkUnitJobs;
    }
    
	@Override
	public ArrangementResult processJobResult(AnalysisResult result) {
		ArrangementResult job = findJobByID(result.getJobID());
		AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(result.getClass());
		job.setResult(service.processResult(result));  		
		job.setScreenshot(result.getScreenshot());
		job.setEtalonScreenshot(result.getEtalonScreenshot());
		return arrangementResultRepo.save(job);
	}

	@Override
	public ArrangementStatus checkArrangementStatus(Long arramgementID) {
		Long notFinishedJobsCount = arrangementResultRepo.countByResultNullOrResultIn(
			Arrays.asList(
				CheckUnitJobResult.RUNNING,
				CheckUnitJobResult.CAPTCHA_DETECTED)
			);
		return notFinishedJobsCount > 0 ? ArrangementStatus.RUNNING : ArrangementStatus.FINISHED;
	}
    
    private Long saveCheckUnitJobAsResult(Long arrangementID, Long erdiID, CheckUnitJob checkUnitJob) {
        try{
            ArrangementResult arrangementResult = new ArrangementResult();
            arrangementResult.setResult(CheckUnitJobResult.RUNNING);
            arrangementResult.setArrangementId(arrangementID);
            arrangementResult.setErdiId(erdiID);
            arrangementResult.setCheckUnitType(checkUnitJob.getCheckUnit().getType());
            arrangementResult.setCheckUnitValue(checkUnitJob.getCheckUnit().getValue());
            return arrangementResultRepo.save(arrangementResult).getId();
        }catch (Exception ex){
            throw new AS_15_8_DispatcherException("Ошибка сохранения задания на проверку запрещенного ресурса!", ex);
        }
    }
    
    class CheckUnitJobMapper{
    	
    	private AccessToolUnit accessToolUnit;
    	private Map<AccessToolParameters, String> parameters;
    	
    	public CheckUnitJobMapper(AccessToolUnit accessToolUnit, Map<AccessToolParameters, String> parameters) {
			this.accessToolUnit = accessToolUnit;
			this.parameters = parameters;
		}
    	
        public CheckUnitJob map(ResultSet rs, int rowNum) throws SQLException {
            CheckUnitJob checkUnitJob = new CheckUnitJob();
            checkUnitJob.setAccessToolUnit(accessToolUnit);
            CheckUnitType type = CheckUnitType.valueOf(rs.getString("check_unit_type"));
            checkUnitJob.setCheckUnit(new CheckUnit(type, rs.getString("check_unit_value")));
            checkUnitJob.getAccessToolParameters().putAll(parameters);
            return checkUnitJob;
        }
    }

	@Override
	public ArrangementResult updateJobStatus(Long jobID, CheckUnitJobResult status) {
		ArrangementResult job = findJobByID(jobID);
		job.setResult(status);
		return arrangementResultRepo.save(job);
	}
	
	private ArrangementResult findJobByID(Long jobID) {
		return arrangementResultRepo.findById(jobID)
			.orElseThrow(() -> 
				new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
			);
	}
}
