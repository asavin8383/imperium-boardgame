package services;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import repositories.ParameterRepository;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParameterService {

    private final ParameterRepository parameterRepository;

    public String getParamValue(String name){
        return getParamValue(name, true);
    }

    public String getParamValue(String name, Boolean enabledParam){
        if (StringUtils.isEmpty(name))
            return null;
        if (enabledParam == null)
            return parameterRepository.getParameterValue(name);
        else if (enabledParam)
            return parameterRepository.getEnabledParameterValue(name);
        else
            return parameterRepository.getDisabledParameterValue(name);
    }
}
