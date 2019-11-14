package service;

import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitVerificationServiceFactory {

    private final ObjectProvider<List<CheckUnitVerificationService>> verificationServices;

    public CheckUnitVerificationService getService(CheckUnitType checkUnitType){
        List<CheckUnitVerificationService> services = verificationServices.getIfAvailable();
        for(CheckUnitVerificationService service : services){
            if(service.getCheckUnitTypes().contains(checkUnitType))
                return service;
        }
        throw new RuntimeException("Ошибка! Тип запрещенного ресурса " + checkUnitType + " не поддерживается");
    }

}
