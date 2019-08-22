package controllers.handlers;

import exceptions.AS_15_8_API_Error;
import exceptions.AS_15_8_Exception;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Creation date: 22.08.2019
 * Author: asavin
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AS_15_8_Exception.class)
    protected ResponseEntity<Object> handleAS_15_8_Exception(
            AS_15_8_Exception ex) {
        AS_15_8_API_Error apiError = new AS_15_8_API_Error(HttpStatus.BAD_REQUEST, ex.getMessage());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(AS_15_8_API_Error ex) {
        return new ResponseEntity<>(ex, ex.getStatus());
    }
}
