package enums;

public enum Dictionary {

    ERDI("ЕРДИ", "Записи ЕРДИ"),
    PS("ПС", "Поисковые системы"),
    PASD("ПАСД", "Программно-аппаратные средства доступа"),
    SUBTYPE("Типы нарушений", "Типы нарушений"),
    USER_ERDI("Пользовательские ЕРДИ", "Пользовательские записи ЕРДИ"),
    SEARCH_PHRASES("Поисковые фразы", "Поисковые фразы");

    private String shortName;
    private String fullName;

    Dictionary(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public int getId() {
        return ordinal() + 1;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }
}
