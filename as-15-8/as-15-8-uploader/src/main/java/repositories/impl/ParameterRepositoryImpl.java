package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import repositories.ParameterRepository;
import javax.persistence.EntityManager;



@Repository
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ParameterRepositoryImpl {

    private EntityManager em;
    private ParameterRepository parameterRepository;


    public String getEnabledParameterValue(String name){
        return parameterRepository.getParameterValue(name);
    }

    public String getDisabledParameterValue(String name){
        return parameterRepository.getDisabledParameterValue(name);
    }

    public String getParameterValue(String name){
        return parameterRepository.getParameterValue(name);
    }

    public void setParameterValue(String name, String value, boolean enabled){
        Parameter parameter = parameterRepository.findByNameAndEnabled(name, enabled);
        if (parameter == null){
            parameter = new Parameter();
        }
        parameter.setName(name);
        parameter.setValue(value);
        parameter.setEnabled(enabled);

        parameterRepository.save(parameter);
    }

    public void setParameterValue(String name, String value){
        setParameterValue(name, value, true);
    }
}
