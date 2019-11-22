package service;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ExecutorProperties;
import enums.AccessToolUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robots.exceptions.ExecutionException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitVerificationServiceFactory {

    private final ExecutorProperties executorProperties;
    private final ObjectProvider<List<CheckUnitVerificationService>> verificationServices;

    public CheckUnitVerificationService getService(CheckUnitJob checkUnitJob){
        List<CheckUnitVerificationService> services = verificationServices.getIfAvailable();
        AccessToolUnit accessToolUnit = executorProperties.getAccessToolUnit(checkUnitJob.getAccessTool())
                .orElseThrow(() -> {
                    throw new RuntimeException("Ошибка получения сервиса для выполнения проверки. ПС/ПАСД не определен в системе: " + checkUnitJob.getAccessTool());
                });
        for(CheckUnitVerificationService service : services){
            if(service.getCheckUnitTypes().contains(checkUnitJob.getCheckUnit().getType()) &&
                service.getAccessToolUnits().contains(accessToolUnit))
                    return service;
        }
        throw new RuntimeException("Ошибка! Тип запрещенного ресурса " + checkUnitJob.getCheckUnit().getType() + " для проверки в ПС/ПАСД "+checkUnitJob.getAccessTool()+" не поддерживается");
    }

}
