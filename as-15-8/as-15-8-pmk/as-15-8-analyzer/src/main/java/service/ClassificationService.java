package service;

import model.NLPCategory;
import model.NLPModel;

public interface ClassificationService {

    NLPCategory classify(String content, NLPModel model);

}
