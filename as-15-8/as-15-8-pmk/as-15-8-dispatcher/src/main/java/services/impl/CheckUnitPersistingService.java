package services.impl;

import checkUnits.CheckUnitJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ArrangementResultRepository;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitPersistingService {

    private final ArrangementResultRepository arrangementResultRepository;

    public ArrangementResult persistCheckUnitJob(CheckUnitJob checkUnitJob){
        ArrangementResult arrangementResult = new ArrangementResult();
        arrangementResult.setArrangementId(checkUnitJob.getArrangementId());
        arrangementResult.setErdiId(checkUnitJob.getCheckUnit().getErdiId());
        arrangementResult.setCheckUnitType(checkUnitJob.getCheckUnit().getType());
        arrangementResult.setCheckUnitValue(checkUnitJob.getCheckUnit().getValue());
        return arrangementResultRepository.save(arrangementResult);
    }

}
