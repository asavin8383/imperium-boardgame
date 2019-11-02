package services.impl;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.DispatcherProperties;
import enums.AccessToolParameter;
import enums.AccessToolUnit;
import exceptions.AS_15_8_DispatcherException;
import jobs.ArrangementJob;
import jobs.ERDIJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ScheduleCheckUnit;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import repositories.ScheduleCheckUnitRepo;
import services.CheckUnitSchedulingService;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class CheckUnitSchedulingServiceImpl implements CheckUnitSchedulingService {

    private final NamedParameterJdbcTemplate jdbcNamedTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final DispatcherProperties dispatcherProperties;

    private final List<Protocol> protocols = new ArrayList<>();

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
    public void createScheduleCheckUnits(ArrangementJob arrangementJob) {
        prepareJobsForStart(arrangementJob);
    }

    private List<CheckUnitJob> prepareJobsForStart(ArrangementJob arrangementJob) {
    	//Удаляем результаты, если они есть и это допустимо
		for(ERDIJob erdiJob: arrangementJob.getErdiJobList())
		{
			deleteArrangementResults(arrangementJob.getId(), erdiJob.getId());
		}

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids",
                arrangementJob.getErdiJobList().stream()
                .mapToLong(ERDIJob::getId)
                .boxed()
                .collect(Collectors.toList())
        );

        CheckUnitJobMapper mapper = new CheckUnitJobMapper(arrangementJob.getAccessTool(), arrangementJob.getAccessToolParameters());
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
        if(checkUnitJobcheckUnitJobTemplates.size()==0){
        	throw new AS_15_8_DispatcherException("Ошибка заполнения мероприятия " + arrangementJob.getId() + " ресурсами. Не было найдено ни одного ресурса для ЕРДИ "
					+ arrangementJob.getErdiJobList().stream().map(erdiJob -> erdiJob.getId().toString()).collect(Collectors.joining(",")));
		}
		log.info("Для мероприятия " + arrangementJob.getId() + " был подготовлен список шаблонов из " + checkUnitJobcheckUnitJobTemplates.size() + " элементов");
        return buildCheckUnitJobsFromTemplates(checkUnitJobcheckUnitJobTemplates, arrangementJob.getId());
    }

    /**
     * Построение проверок по шаблонам
     * @param checkUnitJobTemplates список шаблонов
     * @return список заданий на проверки
     */
    //TODO Исправить ЕРДИ айди!!!
    private List<CheckUnitJob> buildCheckUnitJobsFromTemplates(List<CheckUnitJobTemplate> checkUnitJobTemplates, Long arrangementId){
        List<CheckUnitJob> checkUnitJobs = new ArrayList<>();
        for(CheckUnitJobTemplate template : checkUnitJobTemplates){
        	AccessToolUnit accessToolUnit = AccessToolUnit.fromPropertyKey(dispatcherProperties.getAccessTools().get(template.getAccessTool()));
        	boolean isPS = accessToolUnit == AccessToolUnit.SEARCH_SYSTEM ||
					accessToolUnit == AccessToolUnit.GOOGLE_API;
        	
            switch (template.checkUnitType) {
                case URL:
                	checkUnitJobs.add(createAndSaveCheckUnitJob(
                			arrangementId,
                			template, 
                			new CheckUnit(1L, template.checkUnitType,
                                template.checkUnitValueTemplate)));
                    break;
                case DOMAIN:
                	if(isPS) {
                		checkUnitJobs.add(createAndSaveCheckUnitJob(
                    			arrangementId,
                    			template, 
                    			new CheckUnit(1L, template.checkUnitType,
                            		template.checkUnitValueTemplate)));
                	} else {
	                    for (Protocol protocol : protocols){    
	                    	checkUnitJobs.add(createAndSaveCheckUnitJob(
	                        			arrangementId,
	                        			template, 
	                        			new CheckUnit(1L, template.checkUnitType,
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
                            			new CheckUnit(1L, CheckUnitType.DOMAIN,
                                        		domainName)));
                        	} else {
	                            for (Protocol protocol : protocols) {  
	                            	checkUnitJobs.add(createAndSaveCheckUnitJob(
	                            			arrangementId,
	                            			template, 
	                            			new CheckUnit(1L, CheckUnitType.DOMAIN,
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
                    			new CheckUnit(1L, template.checkUnitType,
                                		protocol.getProtocol() + template.checkUnitValueTemplate + ":" + protocol.getPort())));
                    }
                    break;
                case IP_V6:
                	for (Protocol protocol : protocols){   
                    	checkUnitJobs.add(createAndSaveCheckUnitJob(
                    			arrangementId,
                    			template, 
                    			new CheckUnit(1L, template.checkUnitType,
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
                        			new CheckUnit(1L, CheckUnitType.IP_V4,
                                    		protocol.getProtocol() + address + ":" + protocol.getPort())));
                        }
                    }
                default:
                    break;

            }
        }
        log.info("Для мероприятия " + arrangementId + " был подготовлен список из " + checkUnitJobs.size() + " элементов");
        return checkUnitJobs;
    }
    



    private Long saveScheduleCheckUnit(Long arrangementID, Long erdiID, CheckUnitJob checkUnitJob) {
        try{
            ScheduleCheckUnit scheduleCheckUnit = new ScheduleCheckUnit();
            scheduleCheckUnit.setArrangementId(arrangementID);
            scheduleCheckUnit.setErdiId(erdiID);
            scheduleCheckUnit.setCheckUnitType(checkUnitJob.getCheckUnit().getType());
            scheduleCheckUnit.setCheckUnitValue(checkUnitJob.getCheckUnit().getValue());
            return scheduleCheckUnitRepo.save(scheduleCheckUnit).getId();
        }catch (Exception ex){
            throw new AS_15_8_DispatcherException("Ошибка сохранения задания на проверку запрещенного ресурса!", ex);
        }
    }

	/**
	 * Проверка доступности удаления списка ресурсов мероприятия. (Только для статуса NEW)
	 * @param arrangementId идентификатор мероприятия
	 * @return можно ли удалить список ресурсов мероприятия
	 */
	private boolean checkArrangementStatusForDelete(Long arrangementId){
    	String sql = "select status from portal.arrangements where id = ?";
    	String arrangementStatus = jdbcTemplate.queryForObject(sql, new Object[] { arrangementId }, String.class);
		if(arrangementStatus != null && (arrangementStatus.equals("NEW"))){
			return true;
		}
    	return false;
	}

	void deleteArrangementResults(Long arrangementId, Long erdiId){
		try{
			if(!checkArrangementStatusForDelete(arrangementId)){
				throw new AS_15_8_DispatcherException("Ошибка удаления списка запрещенных ресурсов мероприятия! Мероприятие " + arrangementId + " имеет недопустимый статус (запущено или завершено)");
			}
			scheduleCheckUnitRepo.deleteByArrangementIdAndErdiId(arrangementId, erdiId);
		}catch (EmptyResultDataAccessException ex){
			log.info("У мероприятия "  + arrangementId + " для ЕРДИ " + erdiId + " нет списка ресурсов");
		}
		catch (Exception ex){
			log.error("Ошибка удаления списка запрещённых ресурсов мероприятия " + arrangementId + " для ЕРДИ " + erdiId,ex);
			throw new AS_15_8_DispatcherException("Ошибка удаления списка запрещенных ресурсов мероприятия!", ex);
		}

	}


	
	private CheckUnitJob createAndSaveCheckUnitJob(Long arrangementID, CheckUnitJobTemplate template, CheckUnit checkUnit) {
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setAccessTool(template.getAccessTool());
        checkUnitJob.setCheckUnit(checkUnit);
        Long ipJobID = saveScheduleCheckUnit(arrangementID, template.erdiId, checkUnitJob);
        checkUnitJob.setJobID(ipJobID);
        return checkUnitJob;
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
		private String accessTool;
		private final Map<AccessToolParameter, String> parameters = new HashMap<>();
		private CheckUnitType checkUnitType;
		private String checkUnitValueTemplate;
	}
	
	static class CheckUnitJobMapper{
		
		private String accessTool;
		private Map<AccessToolParameter, String> parameters;
		
		CheckUnitJobMapper(String accessTool, Map<AccessToolParameter, String> parameters) {
			this.accessTool = accessTool;
			this.parameters = parameters;
		}
		
		CheckUnitJobTemplate map(ResultSet rs, int rowNum) throws SQLException {
			CheckUnitJobTemplate checkUnitJobTemplate = new CheckUnitJobTemplate();
			checkUnitJobTemplate.setErdiId(rs.getLong("id"));
			checkUnitJobTemplate.setAccessTool(accessTool);
			checkUnitJobTemplate.setCheckUnitType(CheckUnitType.valueOf(rs.getString("check_unit_type")));
			checkUnitJobTemplate.setCheckUnitValueTemplate(rs.getString("check_unit_value"));
			checkUnitJobTemplate.getParameters().putAll(parameters);
			return checkUnitJobTemplate;
		}
	}
}
