package execution;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import jobs.CheckUnit;
import lombok.Data;

@Data
@JsonTypeInfo(include=As.WRAPPER_OBJECT, use=Id.CLASS)
public abstract class ExecutionJobResult {

	/** Идентификатор мероприятия */
	private String arrangenmentID;
	
	/** Идентификатор ЕРДИ */
	private String erdiID;
	
	/** Единица ЕРДИ для проверки */
	private CheckUnit checkUnit;
	
}
