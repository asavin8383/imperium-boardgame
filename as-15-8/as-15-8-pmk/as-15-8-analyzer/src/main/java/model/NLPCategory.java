package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NLPCategory {
    ERROR("Обнаружена ошибка загрузки страницы"),
    STUB("Обнаружена заглушка"),
    NO_STUB("Обнаружен контент страницы"),
    EXCEPTION("Ошибка при категоризации контента"),
    CAPTCHA("Обнаружена CAPTCHA"),
    NOT_CAPTCHA("CAPTCHA не обнаружена");

    @Getter
    private final String description;

    public static NLPCategory parse(String text, NLPCategory def) {
        for (NLPCategory b : NLPCategory.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return def;
    }
}
