package service;

import jobs.ArrangementJob;

/**
 * Интерфейс сервиса управления роботами
 * @author shabalinAI
 *
 */
public interface RobotsService {

	/**
	 * Метод запуска роботов по заданию на проверку мероприятия
	 * @param arrangementJob Идентификатор мероприятия
	 * @return
	 */
	boolean run(ArrangementJob arrangementJob);
	
}
