package repositories.impl;

import lombok.RequiredArgsConstructor;
import model.scheme.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import repositories.ParameterRepository;


@Repository
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ParameterRepositoryExtend {

    //private final EntityManager em;
    private final ParameterRepository parameterRepository;


    public String getParameterValue(String name){
        return parameterRepository.getParameterValue(name);
    }

    public void setParameterValue(String name, String value, boolean enabled){

        //parameterRepository.updateParameter(name, value);

        Parameter parameter = parameterRepository.findByName(name);
        if (parameter == null){
            parameter = new Parameter();
        }
        parameter.setName(name);
        parameter.setValue(value);

        parameterRepository.save(parameter);
    }

    public void setParameterValue(String name, String value){
        setParameterValue(name, value, true);
    }
}
