package service;


import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import execution.ExecutionJobResult;

/**
 * Фабрика сервисов анализа результатов проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Service
@Qualifier("analyzerServiceFactory")
public class AnalyzerServiceFactory {

	/** Список сервисов */
	@Autowired
	private List<AnalyzerService<? extends ExecutionJobResult>> services;
	
	/** Кэш сервисов */
	private static final Map<Class<? extends ExecutionJobResult>, AnalyzerService<? super ExecutionJobResult>> servicesCache = new HashMap<>();
	
	/**
	 * Метод создания кэша сервисов 
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initServicesCache() {
		services.forEach(service -> 
		servicesCache.put(
				(Class<? extends ExecutionJobResult>) ((ParameterizedType)service.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0],
				(AnalyzerService<? super ExecutionJobResult>) service)
		);
	}
	
	/**
	 * Метод получения сервиса
	 * @param serviceType Тип сервиса
	 * @return
	 */
	public static AnalyzerService<? super ExecutionJobResult> getService(Class<? extends ExecutionJobResult> serviceType) {
		AnalyzerService<? super ExecutionJobResult> service = servicesCache.get(serviceType);
		if(service == null) {
			throw new IllegalArgumentException("Error creating analyzer service! Service for " + serviceType + " is not supported");
		}
		return service;
	}
	
}
