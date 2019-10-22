package services;

import analysis.AnalysisResult;
import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import jobs.ArrangementJob;
import model.ArrangementResult;

import java.util.List;

/**
 * Сервис работы с заданиями на проверку запрещенных ресурсов
 * Author: asavin
 */
public interface CheckUnitSchedulingService {

    void createScheduleCheckUnits(ArrangementJob arrangementJob);

}
