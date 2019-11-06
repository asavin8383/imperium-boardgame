package services.impl;

import org.springframework.beans.factory.annotation.Autowired;

import exceptions.AS_15_8_DispatcherException;
import model.Result;
import repositories.ResultRepo;

/**
 * Базовый класс сервиса обработки анализа результатов проверки
 * @author shabalinAI
 *
 */
public abstract class BaseAnalysisResultService {

	@Autowired
	private ResultRepo resultRepo;
	
	protected void updateJob(Result job) {
		resultRepo.save(job);
	}
	
	protected Result findJob(Long jobID) {
		return resultRepo.findById(jobID)
			.orElseThrow(() -> 
				new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: "+jobID)
			);
	}
}
