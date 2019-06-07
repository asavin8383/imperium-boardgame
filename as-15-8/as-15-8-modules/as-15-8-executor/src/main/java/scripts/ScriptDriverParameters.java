package scripts;

import java.net.URL;

import org.openqa.selenium.Platform;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScriptDriverParameters {

	private URL hubURL;

	private Platform platformName;

	private String applicationName;

	private String browserName;
	
}
