package aspects;

import java.util.Optional;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import exceptions.ResourceNotFoundException;

@Aspect
@Component
public class InvalidRepositoryReturnValueAspect {

	@AfterReturning(pointcut = "execution(* controllers.*Controller.getSingle*(..))", returning = "result")
	public void intercept(final Object result) {
		if (result == null || result.equals(Optional.empty())) {
			throw new ResourceNotFoundException("Queried entity was not found");
		}
	}
}
