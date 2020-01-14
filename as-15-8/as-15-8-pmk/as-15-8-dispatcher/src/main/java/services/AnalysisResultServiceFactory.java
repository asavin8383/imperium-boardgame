package services;


import analysis.CheckUnitResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика сервисов анализа результатов проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Service
public class AnalysisResultServiceFactory {

	/** Список сервисов */
	@Autowired
	private List<AnalysisResultService<? extends CheckUnitResult>> services;
	
	/** Кэш сервисов */
	private static final Map<Class<? extends CheckUnitResult>, AnalysisResultService<? super CheckUnitResult>> servicesCache = new HashMap<>();
	
	/**
	 * Метод создания кэша сервисов 
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initServicesCache() {
		services.forEach(service -> 
			servicesCache.put(
				(Class<? extends CheckUnitResult>) ((ParameterizedType)service.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0],
				(AnalysisResultService<? super CheckUnitResult>) service)
		);
	}
	
	/**
	 * Метод получения сервиса
	 * @param serviceType Тип сервиса
	 * @return
	 */
	public static AnalysisResultService<? super CheckUnitResult> getService(Class<? extends CheckUnitResult> serviceType) {
		AnalysisResultService<? super CheckUnitResult> service = servicesCache.get(serviceType);
		if(service == null) {
			throw new IllegalArgumentException("Error creating analysis result service! Service for " + serviceType + " is not supported");
		}
		return service;
	}
	
}
