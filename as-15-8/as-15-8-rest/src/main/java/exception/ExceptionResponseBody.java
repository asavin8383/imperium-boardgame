package exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;

//TODO Перенести в единый пакет

@Data
public class ExceptionResponseBody {

	private HttpStatus status;
    private String message;
    private List<String> errors;
 
    public ExceptionResponseBody(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        this.errors = new ArrayList<>();
    }
 
    public ExceptionResponseBody(HttpStatus status, String message, String error) {
        this(status, message);
        this.errors.add(error);
    }
 
    public ExceptionResponseBody(HttpStatus status, String message, List<String> errors) {
        this(status, message);
        this.errors = errors;
    }
}
