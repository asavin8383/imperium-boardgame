package model;

public enum NLPCategory {
    ERROR,
    STUB,
    NO_STUB,
    EXCEPTION
    ;

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
