package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NLPModel {

    PAGE_CONTENT_CLASSIFICATOR("Классификатор содержимого страницы", "classpath:classification.bin"),
    CAPTCHA_DETECTOR("Определитель CAPTCHA", "classpath:captcha_detector.bin");

    @Getter
    private final String description;
    @Getter
    private final String modelPath;
}
