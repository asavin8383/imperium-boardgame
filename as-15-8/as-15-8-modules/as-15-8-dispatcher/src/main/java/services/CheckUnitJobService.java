package services;

import java.util.List;

import analysis.AnalysisResult;
import checkUnits.CheckUnitJob;
import enums.ArrangementStatus;
import jobs.ArrangementJob;
import model.ArrangementResult;

/**
 * Сервис работы с заданиями на проверку запрещенных ресурсов
 * Author: asavin
 */
public interface CheckUnitJobService {

    List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob);

    ArrangementResult processJobResult(AnalysisResult result);
    
    ArrangementStatus checkArrangementStatus(Long arramgementID);
    
}
