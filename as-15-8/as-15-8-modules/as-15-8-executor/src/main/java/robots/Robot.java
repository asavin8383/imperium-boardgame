package robots;

import org.testng.xml.XmlTest;

import checkUnits.CheckUnit;
import enums.AccessToolUnit;

/**
 * Интерфейс робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public interface Robot {
	
	/**
	 * Проверяемая ПС/ПАСД
	 * @return
	 */
	AccessToolUnit getAccessToolUnit();
	
	/**
	 * Метод создания теста для проверки
	 * @param name Имя теста
	 * @param arrangenmentID Идентификатор мероприятия
	 * @param erdiID Идентификатор ЕРДИ
	 * @param url Проверяемый URL
	 * @return
	 */
	XmlTest createTest(String name, Long arrangenmentID, Long erdiID, CheckUnit checkUnit);
	
}
