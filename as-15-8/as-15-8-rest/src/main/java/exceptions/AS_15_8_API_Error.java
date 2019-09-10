package exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;


@Data
public class AS_15_8_API_Error {

	private HttpStatus status;
    private String message;
    private List<String> errors;
 
    public AS_15_8_API_Error(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        this.errors = new ArrayList<>();
    }
 
    public AS_15_8_API_Error(HttpStatus status, String message, Throwable error) {
        this(status, message);
        this.errors.add(error.getLocalizedMessage());
    }
 
    public AS_15_8_API_Error(HttpStatus status, String message, List<String> errors) {
        this(status, message);
        this.errors = errors;
    }
}
