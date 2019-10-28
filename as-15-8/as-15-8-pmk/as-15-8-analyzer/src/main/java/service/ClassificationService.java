package service;

import model.NLPCategory;

public interface ClassificationService {

    NLPCategory classify(String content);

}
