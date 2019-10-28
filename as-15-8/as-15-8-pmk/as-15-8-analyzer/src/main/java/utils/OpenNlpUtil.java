package utils;

import exception.NlpException;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizer;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OpenNlpUtil {

    //Пороговое значение, от которого считаем классификацию успешной
    //private static final double THRESHOLD = .52d;

    public static String categorize(String text, URL modelUrl) throws NlpException {
        try{
            DoccatModel model = new DoccatModel(modelUrl);
            DocumentCategorizer doccat = new DocumentCategorizerME(model);
            String[] textTokens =  tokenize(text);
            double[] aProbs = doccat.categorize(textTokens);
            String resultString = doccat.getBestCategory(aProbs);
            double weight = aProbs[doccat.getIndex(resultString)];
            /*if (weight < THRESHOLD) {
                return OrganizationType.UNKNOWN;
            }*/

            return resultString;

        } catch (Exception ex){
            throw new NlpException("Ошибка при категоризации текста!", ex);
        }
    }

    public static String[] findNamedObjects(String text, String modelPath, String objectName){
        try {
            TokenNameFinderModel model;
            try(InputStream modelIn = OpenNlpUtil.class.getClassLoader().getResourceAsStream(modelPath)) {
                model = new TokenNameFinderModel(modelIn);
            }
            NameFinderME nameFinder = new NameFinderME(model);
            String[] sentenceTokens = tokenize(text);
            List<Span> spans = Arrays.stream(nameFinder.find(sentenceTokens))
                    .filter(span -> span.getType().equals(objectName))
                    .collect(Collectors.toList());

            return Span.spansToStrings(spans.toArray(new Span[spans.size()]), sentenceTokens);
        } catch (Exception ex){
            throw new NlpException("Ошибка при поиске "+objectName+" в тексте!", ex);
        }
    }

    private static String[] tokenize(String sentence) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        return tokenizer.tokenize(sentence);
    }

    private static String[] splitToSentences(String text) throws IOException {
        InputStream is = OpenNlpUtil.class.getResourceAsStream("sentence.bin");
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);
        return sdetector.sentDetect(text);
    }
}
