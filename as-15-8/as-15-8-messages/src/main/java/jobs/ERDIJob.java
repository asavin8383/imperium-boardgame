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

	private Long id;
	
	@Getter
	private List<String> urls = new ArrayList<>();
	
	public void addUrl(String url) {
		urls.add(url);
	}
}
