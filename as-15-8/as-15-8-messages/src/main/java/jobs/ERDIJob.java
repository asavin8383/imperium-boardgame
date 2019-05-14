package jobs;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Задание на проверку ЕРДИ
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
public class ERDIJob {

	/** Идентификатор ЕРДИ */
	private Long id;
	
	/** Список URL ЕРДИ для проверки */
	@Getter
	private List<String> urls = new ArrayList<>();
	
	/**
	 * Метод добавления URL ЕРДИ в список для проверки
	 * @param url
	 */
	public void addUrl(String url) {
		urls.add(url);
	}
}
