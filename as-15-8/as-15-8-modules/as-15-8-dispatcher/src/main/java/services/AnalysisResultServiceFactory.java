package services;


import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import analysis.AnalysisResult;

/**
 * Фабрика сервисов анализа результатов проверки ПС/ПАСД
 * @author shabalinAI
 * @param <T>
 * @param <T>
 *
 */
@Service
public class AnalysisResultServiceFactory {

	/** Список сервисов */
	@Autowired
	private List<AnalysisResultService<? extends AnalysisResult>> services;
	
	/** Кэш сервисов */
	private static final Map<Class<? extends AnalysisResult>, AnalysisResultService<? super AnalysisResult>> servicesCache = new HashMap<>();
	
	/**
	 * Метод создания кэша сервисов 
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initServicesCache() {
		services.forEach(service -> 
			servicesCache.put(
				(Class<? extends AnalysisResult>) ((ParameterizedType)service.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0],
				(AnalysisResultService<? super AnalysisResult>) service)
		);
	}
	
	/**
	 * Метод получения сервиса
	 * @param serviceType Тип сервиса
	 * @return
	 */
	public static AnalysisResultService<? super AnalysisResult> getService(Class<? extends AnalysisResult> serviceType) {
		AnalysisResultService<? super AnalysisResult> service = servicesCache.get(serviceType);
		if(service == null) {
			throw new IllegalArgumentException("Error creating analysis result service! Service for " + serviceType + " is not supported");
		}
		return service;
	}
	
}
