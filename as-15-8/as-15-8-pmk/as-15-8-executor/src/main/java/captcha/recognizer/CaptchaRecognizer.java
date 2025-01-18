package captcha.recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public interface CaptchaRecognizer {

    String recognize(File file) throws IOException, UnsupportedAudioFileException;

    default AudioInputStream convertToWav(AudioInputStream audioInputStream) {
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

}
