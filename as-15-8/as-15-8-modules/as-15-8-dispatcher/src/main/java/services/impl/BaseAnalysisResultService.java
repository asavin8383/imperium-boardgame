package services.impl;

import org.springframework.beans.factory.annotation.Autowired;

import exceptions.AS_15_8_DispatcherException;
import model.ArrangementResult;
import repositories.ArrangementResultRepository;

/**
 * Базовый класс сервиса обработки анализа результатов проверки
 * @author shabalinAI
 *
 */
public abstract class BaseAnalysisResultService {

	@Autowired
	private ArrangementResultRepository resultRepo;
	
	protected void updateJob(ArrangementResult job) {
		resultRepo.save(job);
	}
	
	protected ArrangementResult findJob(Long jobID) {
		return resultRepo.findById(jobID)
			.orElseThrow(() -> 
				new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: "+jobID)
			);
	}
}
