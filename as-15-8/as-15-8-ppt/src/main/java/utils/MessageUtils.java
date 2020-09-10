package utils;

import exceptions.AS_15_8_PPT_Exception;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    public static void wrapAndThrowUserMessageException(Throwable t){
        Throwable tRres = null;

        if (tRres == null){
            tRres = getDuplicateKeyException(t);
        }

        if (tRres == null){
            tRres = t;
        }
        if (tRres instanceof RuntimeException){
            throw (RuntimeException)tRres;
        }
        else {
            throw new RuntimeException(t);
        }
    }

    public static Throwable getDuplicateKeyException(Throwable e){
        Throwable fnd = e;
        int depth = 3;
        while(--depth >= 0 && (fnd != null) && !(fnd instanceof ConstraintViolationException)){
            fnd = fnd.getCause();
        }

        if (fnd != null && depth >= 0){
            ConstraintViolationException ce = (ConstraintViolationException) fnd;
            SQLException sqle = ce.getSQLException();
            if (sqle != null){
                String str = sqle.getMessage();
                if (str != null && str.contains("duplicate key")){
                    String message = "Обнаружен дубликат поля";

                    Matcher m = Pattern.compile("Key \\((.+?)\\)=\\((.+)\\)", Pattern.DOTALL)
                            .matcher(str);
                    if(m.find()) {
                        String field = m.group(1);
                        String value = m.group(2);
                        message = "Обнаружен дубликат: поле (" + field + ") со значением (" + value + ") уже существует";
                    }
                    return new AS_15_8_PPT_Exception(message, fnd);
                }
            }
        }

        return e;
    }
}
