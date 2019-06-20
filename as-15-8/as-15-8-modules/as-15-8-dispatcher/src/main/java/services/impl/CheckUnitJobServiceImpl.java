package services.impl;

import analysis.AnalysisResult;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import enums.ArrangementStatus;
import enums.CheckUnitJobResult;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import model.ArrangementResult;
import model.DetailResultsVpn;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import repositories.ArrangementResultRepository;
import repositories.DetailResultsVpnRepository;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.CheckUnitJobService;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */
@Service
public class CheckUnitJobServiceImpl implements CheckUnitJobService {

    private NamedParameterJdbcTemplate jdbcNamedTemplate;
    private JdbcTemplate jdbcTemplate;
    private ArrangementResultRepository arrangementResultRepo;
    private DetailResultsVpnRepository detailResultsRepo;
    private final List<Protocol> protocols = new ArrayList<>();

    @Autowired
    public CheckUnitJobServiceImpl(NamedParameterJdbcTemplate jdbcNamedTemplate,
                                   JdbcTemplate jdbcTemplate,
                                   ArrangementResultRepository arrangementResultRepo,
                                   DetailResultsVpnRepository detailResultsRepo) {
        this.jdbcNamedTemplate = jdbcNamedTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.arrangementResultRepo = arrangementResultRepo;
        this.detailResultsRepo = detailResultsRepo;
    }

    @PostConstruct
    private void fillProtocolsAndPorts(){
        String sqlForProtocols = "select id, protocol, port from dictionaries.protocols";
        protocols.addAll(jdbcTemplate.queryForList(sqlForProtocols).stream()
                .map(stringObjectMap -> 
                	new Protocol(
            			stringObjectMap.get("protocol").toString(),
            			Integer.parseInt(stringObjectMap.get("port").toString())
                	)
                )
                .collect(Collectors.toList()));
        /*String sqlForPorts = "select id, port_number from dictionaries.ports";
        ports.addAll(jdbcTemplate.queryForList(sqlForPorts).stream()
                .map(stringObjectMap -> stringObjectMap.get("port_number").toString())
                .collect(Collectors.toList()));*/
    }

    @Override
    public List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob) {
        switch (arrangementJob.getRunType()){
            case START:
                return prepareJobsForStart(arrangementJob);
            default:
                throw new AS_15_8_DispatcherException("Error preparing check unit jobs! Arrangement run type is not supported: " + arrangementJob.getRunType());
        }
    }

    private List<CheckUnitJob> prepareJobsForStart(ArrangementJob arrangementJob) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids",
                arrangementJob.getErdiJobList().stream()
                .mapToLong(ERDIJob::getId)
                .boxed()
                .collect(Collectors.toList())
        );

        CheckUnitJobMapper mapper = new CheckUnitJobMapper(arrangementJob.getAccessToolUnit(), arrangementJob.getAccessToolParameters());
        List<CheckUnitJobTemplate> checkUnitJobcheckUnitJobTemplates;
        try {
            //noinspection SqlDialectInspection
            /*CheckUnitJob job = mapper.map(rs, rowNum);
            Long jobID = saveCheckUnitJobAsResult(arrangementJob.getId(), rs.getLong("id"), job);
            job.setJobID(jobID);
            return job;*/
            checkUnitJobcheckUnitJobTemplates = jdbcNamedTemplate.query(
                "select content.id as id, 'DOMAIN' as check_unit_type, domain.domain as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.domain on content.id = domain.content_id and content.id IN (:ids) and blocktype = 'domain'\n" +
                        "UNION\n" +
                        "select content.id as id, 'DOMAIN_MASK' as check_unit_type, domain.domain as check_unit_value\n" +
                        "  from sa.content\n" +
                        "  join sa.domain on content.id = domain.content_id and content.id IN (:ids) and blocktype = 'domain-mask'\n" +
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
                    mapper::map
            );

        } catch (Exception ex){
        	throw new AS_15_8_DispatcherException("Ошибка при создании заданий на проверку запрещенных ресурсов!", ex);
        }
        return buildCheckUnitJobsFromTemplates(checkUnitJobcheckUnitJobTemplates, arrangementJob.getId());
    }

    /**
     * Построение проверок по шаблонам
     * @param checkUnitJobTemplates список шаблонов
     * @return список заданий на проверки
     */
    private List<CheckUnitJob> buildCheckUnitJobsFromTemplates(List<CheckUnitJobTemplate> checkUnitJobTemplates, Long arrangementId){
        List<CheckUnitJob> checkUnitJobs = new ArrayList<>();
        for(CheckUnitJobTemplate template : checkUnitJobTemplates){
        	
        	boolean isPS = template.getAccessToolUnit() == AccessToolUnit.GOOGLE ||
        			template.getAccessToolUnit() == AccessToolUnit.GOOGLE_API ||
        			template.getAccessToolUnit() == AccessToolUnit.YANDEX;
        	
            switch (template.checkUnitType) {
                case URL:
                	checkUnitJobs.add(createAndSaveCheckUnitJob(
                			arrangementId,
                			template, 
                			new CheckUnit(template.checkUnitType,
                                template.checkUnitValueTemplate)));
                    break;
                case DOMAIN:
                	if(isPS) {
                		checkUnitJobs.add(createAndSaveCheckUnitJob(
                    			arrangementId,
                    			template, 
                    			new CheckUnit(template.checkUnitType,
                            		template.checkUnitValueTemplate)));
                	} else {
	                    for (Protocol protocol : protocols){    
	                    	checkUnitJobs.add(createAndSaveCheckUnitJob(
	                        			arrangementId,
	                        			template, 
	                        			new CheckUnit(template.checkUnitType,
	                                		protocol.getProtocol() + template.checkUnitValueTemplate)));
	                    }
                	}
                    break;
                case DOMAIN_MASK:
                    String sql = "SELECT domain_name FROM dictionaries.domain_mask_items dmi\n" +
                            "JOIN sa.domain domain on domain.id = dmi.domain_mask_id and domain.domain=?";
                    jdbcTemplate.queryForList(sql, template.checkUnitValueTemplate).stream()
                        .map(stringObjectMap -> stringObjectMap.get("domain_name").toString())
                        .forEach(domainName -> {
                        	if(isPS) {
                        		checkUnitJobs.add(createAndSaveCheckUnitJob(
                            			arrangementId,
                            			template, 
                            			new CheckUnit(CheckUnitType.DOMAIN,
                                        		domainName)));
                        	} else {
	                            for (Protocol protocol : protocols) {  
	                            	checkUnitJobs.add(createAndSaveCheckUnitJob(
	                            			arrangementId,
	                            			template, 
	                            			new CheckUnit(CheckUnitType.DOMAIN,
	                                        		protocol.getProtocol() + domainName)));
	                            }
                        	}
                        });
                    break;
                case IP_V4:
                    for (Protocol protocol : protocols){   
                    	checkUnitJobs.add(createAndSaveCheckUnitJob(
                    			arrangementId,
                    			template, 
                    			new CheckUnit(template.checkUnitType,
                                		protocol.getProtocol() + template.checkUnitValueTemplate + ":" + protocol.getPort())));
                    }
                    break;
                case IP_V6:
                	for (Protocol protocol : protocols){   
                    	checkUnitJobs.add(createAndSaveCheckUnitJob(
                    			arrangementId,
                    			template, 
                    			new CheckUnit(template.checkUnitType,
                                		protocol.getProtocol() + "[" + template.checkUnitValueTemplate + "]"+ ":" + protocol.getPort() )));
                    }
                    break;
                case IP_V4_SUBNET:
                    for (Protocol protocol : protocols){
                        //Раскладываем маску в IP-адреса
                        SubnetUtils utils = new SubnetUtils(template.checkUnitValueTemplate);
                        for(String address : utils.getInfo().getAllAddresses()){ 
                        	checkUnitJobs.add(createAndSaveCheckUnitJob(
                        			arrangementId,
                        			template, 
                        			new CheckUnit(CheckUnitType.IP_V4,
                                    		protocol.getProtocol() + address + ":" + protocol.getPort())));
                        }
                    }
                default:
                    break;

            }
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
		job.setEndDate(LocalDateTime.now());
		return arrangementResultRepo.save(job);
	}

	@Override
	public ArrangementStatus checkArrangementStatus(Long arrangementID) {
		Long notFinishedJobsCount = arrangementResultRepo.countByResultNullOrResultIn(arrangementID,
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

	@Override
	public ArrangementResult updateJobStatus(Long jobID, CheckUnitJobResult status, String description) {
		ArrangementResult job = findJobByID(jobID);
		job.setResult(status);
		if(status == CheckUnitJobResult.INTERNAL_ERROR)
			saveErrorToDetailResults(jobID, description);
		return arrangementResultRepo.save(job);
	}
	
	private ArrangementResult findJobByID(Long jobID) {
		return arrangementResultRepo.findById(jobID)
			.orElseThrow(() -> 
				new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
			);
	}
	
	private CheckUnitJob createAndSaveCheckUnitJob(Long arrangementID, CheckUnitJobTemplate template, CheckUnit checkUnit) {
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setAccessToolUnit(template.getAccessToolUnit());
        checkUnitJob.getAccessToolParameters().putAll(template.getParameters());
        checkUnitJob.setCheckUnit(checkUnit);
        Long ipJobID = saveCheckUnitJobAsResult(arrangementID, template.erdiId, checkUnitJob);
        checkUnitJob.setJobID(ipJobID);
        return checkUnitJob;
	}
	
	private void saveErrorToDetailResults(Long jobID, String exText) {
		DetailResultsVpn detailResults = new DetailResultsVpn();
		detailResults.setId(jobID);
		detailResults.setResponseError(false);
		detailResults.setStubScoreInfo(exText);
		detailResultsRepo.save(detailResults);
	}
	
	@Data
	@AllArgsConstructor
	private static class Protocol {
		private String protocol;
		private int port;
	}
	
	@Data
	private static class CheckUnitJobTemplate {
		private Long erdiId;
		private AccessToolUnit accessToolUnit;
		private final Map<AccessToolParameters, String> parameters = new HashMap<>();
		private CheckUnitType checkUnitType;
		private String checkUnitValueTemplate;
	}
	
	static class CheckUnitJobMapper{
		
		private AccessToolUnit accessToolUnit;
		private Map<AccessToolParameters, String> parameters;
		
		CheckUnitJobMapper(AccessToolUnit accessToolUnit, Map<AccessToolParameters, String> parameters) {
			this.accessToolUnit = accessToolUnit;
			this.parameters = parameters;
		}
		
		CheckUnitJobTemplate map(ResultSet rs, int rowNum) throws SQLException {
			CheckUnitJobTemplate checkUnitJobTemplate = new CheckUnitJobTemplate();
			checkUnitJobTemplate.setErdiId(rs.getLong("id"));
			checkUnitJobTemplate.setAccessToolUnit(accessToolUnit);
			checkUnitJobTemplate.setCheckUnitType(CheckUnitType.valueOf(rs.getString("check_unit_type")));
			checkUnitJobTemplate.setCheckUnitValueTemplate(rs.getString("check_unit_value"));
			checkUnitJobTemplate.getParameters().putAll(parameters);
			return checkUnitJobTemplate;
		}
	}
}
