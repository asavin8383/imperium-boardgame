package utils;

import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@UtilityClass
public class Utils {

    public static Date getEndDate() {
        return new GregorianCalendar(3000, Calendar.JANUARY, 1).getTime();
    }

}
