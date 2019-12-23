package service;

import checkUnits.CheckUnitJob;
import common.ExecutorProperties;
import enums.AccessToolUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitVerificationServiceFactory {

    private final ExecutorProperties executorProperties;
    private final ObjectProvider<List<CheckUnitVerificationService>> verificationServices;

    public CheckUnitVerificationService getService(CheckUnitJob checkUnitJob){
        List<CheckUnitVerificationService> services = verificationServices.getIfAvailable();
        AccessToolUnit accessToolUnit = executorProperties.getAccessToolUnit(checkUnitJob.getAccessTool())
                .orElseThrow(() ->
                        new RuntimeException("Ошибка получения сервиса для выполнения проверки. ПС/ПАСД не определен в системе: " + checkUnitJob.getAccessTool()));
        assert services != null;
        for(CheckUnitVerificationService service : services){
            if(service.getSupportedTypes().containsKey(accessToolUnit)){
                if(service.getSupportedTypes().get(accessToolUnit).contains(checkUnitJob.getCheckUnit().getType()))
                    return service;
            }
        }
        throw new RuntimeException("Ошибка! Тип запрещенного ресурса " + checkUnitJob.getCheckUnit().getType() + " для проверки в ПС/ПАСД "+checkUnitJob.getAccessTool()+" не поддерживается");
    }

}
