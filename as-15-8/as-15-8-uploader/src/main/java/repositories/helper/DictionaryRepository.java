package repositories.helper;

import model.enums.Dictionary;

import java.time.LocalDateTime;

public interface DictionaryRepository {

    Dictionary getDictionaryType();

    long getCountByEffDt(LocalDateTime effDt);

    LocalDateTime getUpdateDateTime(LocalDateTime effDt);

}
