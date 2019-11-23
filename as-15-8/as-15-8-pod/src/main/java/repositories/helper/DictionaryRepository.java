package repositories.helper;

import enums.Dictionary;

import java.util.Date;

public interface DictionaryRepository {

    Dictionary getDictionaryType();

    long getCountByEffDt(Date effDt);

    Date getUpdateDateTime(Date effDt);

}
