package service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import execution.ExecutionJobResult;

/**
 * Фабрика сервисов анализа результатов проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Service
public class AnalyzerServiceFactory {

	/** Список сервисов */
	@Autowired
	private List<AnalyzerService> services;
	
	/** Кэш сервисов */
	private static final Map<Class<? extends ExecutionJobResult>, AnalyzerService> servicesCache = new HashMap<>();
	
	/**
	 * Метод создания кэша сервисов 
	 */
	@PostConstruct
	public void initServicesCache() {
		services.forEach(service -> servicesCache.put(service.getExecutionResultType(), service));
	}
	
	/**
	 * Метод получения сервиса
	 * @param serviceType Тип сервиса
	 * @return
	 */
	public static AnalyzerService getService(Class<? extends ExecutionJobResult> serviceType) {
		AnalyzerService service = servicesCache.get(serviceType);
		if(service == null) {
			throw new IllegalArgumentException("Error creating analyzer service! Service for " + serviceType + " is not supported");
		}
		return service;
	}
	
}
