package control;

import enums.AccessToolUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorControlMessage {

	private AccessToolUnit accessToolUnit;
	
	private ControlCommand command;
	
	public enum ControlCommand{
		
		START,
		STOP;
		
	}
	
}
