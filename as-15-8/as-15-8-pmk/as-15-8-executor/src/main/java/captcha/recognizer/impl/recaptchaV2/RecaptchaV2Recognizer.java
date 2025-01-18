package captcha.recognizer.impl.recaptchaV2;

import captcha.recognizer.CaptchaRecognizer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
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
public class RecaptchaV2Recognizer implements CaptchaRecognizer {

    private final StreamSpeechRecognizer recognizer;
    public RecaptchaV2Recognizer() throws IOException {
        Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        this.recognizer = new StreamSpeechRecognizer(configuration);
        log.info("Создан распознаватель recaptcha V2");
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
                            .replaceAll("\\sif|\\sit|\\sf\\.|\\sf$", "")
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
        RecaptchaV2Recognizer recognizer = new RecaptchaV2Recognizer();
        String text = recognizer.recognize(file.toFile());
        System.out.println(text);
    }
}
