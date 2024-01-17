package captcha.recognizer.impl.yandex;

import captcha.recognizer.CaptchaRecognizer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value="prototype")
@Slf4j
public class YandexSmartCaptchaRecognizer implements CaptchaRecognizer {

    private final StreamSpeechRecognizer recognizer;

    public YandexSmartCaptchaRecognizer() throws IOException {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("file:D:\\Projects\\Git repositories\\as-15-8\\backend\\as-15-8\\as-15-8-pmk\\as-15-8-executor\\cmusphinx-ru-5.2\\acoustic_model");
        configuration.setDictionaryPath("file:D:\\Projects\\Git repositories\\as-15-8\\backend\\as-15-8\\as-15-8-pmk\\as-15-8-executor\\cmusphinx-ru-5.2\\digits.dict");
        configuration.setLanguageModelPath("file:D:\\Projects\\Git repositories\\as-15-8\\backend\\as-15-8\\as-15-8-pmk\\as-15-8-executor\\cmusphinx-ru-5.2\\digits.lm");

        this.recognizer = new StreamSpeechRecognizer(configuration);
        log.info("Создан распознаватель Yandex Smart Captcha");
    }

    public String recognize(File file) throws IOException, UnsupportedAudioFileException {
        try( AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            try (AudioInputStream stream = convertToWav(audioInputStream)) {
                this.recognizer.startRecognition(stream);
                SpeechResult result;
                List<String> textList = new ArrayList<>();
                while ((result = this.recognizer.getResult()) != null) {
                    textList.add(result
                            .getHypothesis()
                    );
                }
                this.recognizer.stopRecognition();

                return String.join(" ", textList);
            }
        }
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        // Path file = new File("D:\\projects\\git\\as-15-8\\backend\\as-15-8\\as-15-8-pmk\\as-15-8-executor\\audio.mp3").toPath();
        Path file = new File("C:\\Users\\shabalinAI\\Downloads\\voice.mp3").toPath();
        // Path file = new File("D:\\projects\\git\\as-15-8\\selenium-recaptcha-solver\\tst.wav").toPath();
        CaptchaRecognizer recognizer = new YandexSmartCaptchaRecognizer();
        String text = recognizer.recognize(file.toFile());
        System.out.println(text);
    }
}
