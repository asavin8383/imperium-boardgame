package repositories.helper;

import model.enums.Dictionary;

import java.util.Date;

public interface DictionaryRepository {

    Dictionary getDictionaryType();

    long getCountByEffDt(Date effDt);

    Date getUpdateDateTime(Date effDt);

}
