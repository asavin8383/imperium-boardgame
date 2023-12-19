package captcha;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CaptchaUtils {

    public static void downloadFile(String fileUrl, File file) throws IOException, CaptchaSolverException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK ||
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM) {

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(file);

            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
        } else {
            throw new CaptchaSolverException("Не удалось загрузить файл с аудио, статус ответа: " + responseCode);
        }
        httpConn.disconnect();
    }

    public static void convertMp3ToWav(Path mp3FilePath, Path wavFilePath) throws UnsupportedAudioFileException, IOException {

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(mp3FilePath.toFile());
        AudioFormat sourceFormat = audioInputStream.getFormat();

        // convert to PCM 16-bit signed little-endian format (16-bit WAV)
        AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16,
                sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);

        // create output stream
        AudioInputStream pcmAudioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        AudioSystem.write(pcmAudioInputStream, AudioFileFormat.Type.WAVE, wavFilePath.toFile());

        // close streams
        audioInputStream.close();
        pcmAudioInputStream.close();
    }
}
