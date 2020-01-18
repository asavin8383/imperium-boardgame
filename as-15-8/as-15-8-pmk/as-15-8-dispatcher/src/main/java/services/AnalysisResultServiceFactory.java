package services;


import analysis.CheckUnitResult;
import lombok.RequiredArgsConstructor;
import model.DetailResult;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AnalysisResultServiceFactory {

	/** Список сервисов */
	private final List<DetailResultService<? extends CheckUnitResult, ? extends DetailResult>> services;
	
	/** Кэш сервисов */
	private static final Map<Class<? extends CheckUnitResult>, DetailResultService<? super CheckUnitResult, ? extends DetailResult>> servicesCache = new HashMap<>();

	/**
	 * Метод создания кэша сервисов 
	 */
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initServicesCache() {
		services.forEach(service -> 
			servicesCache.put(
				(Class<? extends CheckUnitResult>) ((ParameterizedType)service.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0],
				(DetailResultService<? super CheckUnitResult, ? extends DetailResult>) service)
		);
	}
	
	/**
	 * Метод получения сервиса
	 * @param serviceType Тип сервиса
	 */
	public static DetailResultService<? super CheckUnitResult, ? extends DetailResult> getService(Class<? extends CheckUnitResult> serviceType) {
		DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = servicesCache.get(serviceType);
		if(service == null) {
			throw new IllegalArgumentException("Error creating analysis result service! Service for " + serviceType + " is not supported");
		}
		return service;
	}
	
}
