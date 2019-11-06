package services.impl;

import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ResultRepo;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitPersistingService {

    private final ResultRepo resultRepo;

    public Result persistCheckUnitJob(CheckUnitJob checkUnitJob){
        Result result = new Result();
        result.setArrangementId(checkUnitJob.getArrangementId());
        result.setErdiId(checkUnitJob.getCheckUnit().getErdiId());
        result.setCheckUnitType(checkUnitJob.getCheckUnit().getType());
        result.setCheckUnitValue(checkUnitJob.getCheckUnit().getValue());
        result.setResult(CheckUnitJobResult.RUNNING);
        return resultRepo.save(result);
    }

}
