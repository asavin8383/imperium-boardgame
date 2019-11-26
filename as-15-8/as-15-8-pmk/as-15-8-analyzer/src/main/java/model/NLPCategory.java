package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NLPCategory {
    ERROR("Обнаружена ошибка загрузки страницы"),
    STUB("Обнаружена заглушка"),
    NO_STUB("Обнаружен контент страницы"),
    EXCEPTION("Ошибка при категоризации контента");

    @Getter
    private String description;

    public static NLPCategory parse(String text, NLPCategory def) {
        for (NLPCategory b : NLPCategory.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return def;
    }

    public static NLPCategory parse(String text) {
        return NLPCategory.parse(text, EXCEPTION);
    }
}
