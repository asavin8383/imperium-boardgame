package control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorControlMessage {

	private ControlCommand command;
	
	public enum ControlCommand{
		
		START,
		STOP;
		
	}
	
}
