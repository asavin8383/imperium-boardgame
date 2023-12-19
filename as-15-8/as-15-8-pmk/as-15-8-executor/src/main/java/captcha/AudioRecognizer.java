package captcha;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AudioRecognizer {
    public static String recognize(File file) throws IOException, UnsupportedAudioFileException {
        try( AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            try (AudioInputStream stream = convertToWav(audioInputStream)) {
                Configuration configuration = new Configuration();
                configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
                configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
                configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

                StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
                recognizer.startRecognition(stream);
                SpeechResult result;
                List<String> textList = new ArrayList<>();
                while ((result = recognizer.getResult()) != null) {
                    textList.add(result
                            .getHypothesis()
                            .replaceAll("\\sif|\\sit|\\sf\\.|\\sf$", "")
                    );
                }
                recognizer.stopRecognition();

                return String.join(" ", textList);
            }
        }
    }

    private static AudioInputStream convertToWav(AudioInputStream audioInputStream) {
        // convert to PCM 16-bit signed little-endian format (16-bit WAV)
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    16000, // Частота дискретизации 16 кГц
                    16, // 16 бит на сэмпл
                    1, // Монофонический звук
                    2, // 2 байта на фрейм (little-endian)
                    16000, // Кадров в секунду
                    false // Без выравнивания
            );
        // create output stream
        return AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        Path file = new File("D:\\projects\\git\\as-15-8\\backend\\as-15-8\\as-15-8-pmk\\as-15-8-executor\\audio.mp3").toPath();
        // Path file = new File("D:\\projects\\git\\as-15-8\\selenium-recaptcha-solver\\tst.wav").toPath();
        String text = recognize(file.toFile());
        System.out.println(text);
    }
}
