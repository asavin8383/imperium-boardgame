package service;

import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitVerificationServiceFactory {

    private final List<CheckUnitVerificationService> verificationServices;

    private static Map<CheckUnitType, CheckUnitVerificationService> verificationServicesCache = new HashMap<>();

    @PostConstruct
    private void init(){
        verificationServices.forEach(
                service -> service.getCheckUnitTypes().forEach(
                        checkUnitType -> verificationServicesCache.put(checkUnitType, service)));
    }

    public static CheckUnitVerificationService getService(CheckUnitType checkUnitType){
        CheckUnitVerificationService service = verificationServicesCache.get(checkUnitType);
        if(service == null)
            throw new RuntimeException("Ошибка! Тип запрещенного ресурса " + checkUnitType + " не поддерживается");
        return service;
    }

}
