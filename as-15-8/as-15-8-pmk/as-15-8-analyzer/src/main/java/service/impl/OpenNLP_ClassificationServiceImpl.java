package service.impl;

import exception.ClassificationException;
import exception.NlpException;
import model.NLPCategory;
import model.NLPModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import service.ClassificationService;
import utils.OpenNlpUtil;

import java.net.URL;

@Service
@Qualifier("openNLPClassificator")
public class OpenNLP_ClassificationServiceImpl implements ClassificationService {

    @Override
    public NLPCategory classify(String content, NLPModel model) {
        try {
            URL resourceUrl = new URL(model.getModelPath());
//            URL resourceUrl = OpenNlpUtil.class.getClassLoader().getResource(model.getModelPath());
            if(resourceUrl == null) {
                throw new NlpException("Ошибка при категоризации текста! Не удалось найти дерево принятия решений среди ресурсов по пути " + resourceUrl);
            }
            String resultString = OpenNlpUtil.categorize(content, resourceUrl);
            try {
                return NLPCategory.parse(resultString, NLPCategory.EXCEPTION);
            } catch (IllegalArgumentException ex) {
                throw new ClassificationException("Ошибка категоризации текста! Получен недопустимый вариант при попытке категоризации: " + resultString, ex);
            }

        } catch (Exception ex) {
            throw new ClassificationException("Ошибка при категоризации текста!", ex);
        }
    }
}
