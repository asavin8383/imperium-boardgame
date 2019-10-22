package services;

import model.enums.Dictionary;
import model.projection.DictionaryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.helper.DictionaryRepository;
import utils.Utils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    private Map<Dictionary, DictionaryRepository> repositoryMap;

    @Autowired
    public DictionaryService(List<DictionaryRepository> repositoryList) {
        this.repositoryMap = new EnumMap<>(Dictionary.class);
        for (DictionaryRepository repository : repositoryList) {
            repositoryMap.put(repository.getDictionaryType(), repository);
        }
    }

    public List<DictionaryView> getDictionaryViewList() {
        return repositoryMap.entrySet().stream()
                .map(entry -> new DictionaryView(
                        entry.getKey().toString(),
                        entry.getValue().getCountByEffDt(
                                Utils.getEndDate()),
                        entry.getKey().getShortName()))
                .collect(Collectors.toList());
    }

    public DictionaryView getDictionaryView(Dictionary dictionary) {
        DictionaryRepository repository = repositoryMap.get(dictionary);
        return new DictionaryView(dictionary.toString(), dictionary.getId(),
                dictionary.getShortName(), dictionary.getFullName(),
                repository.getUpdateDateTime(Utils.getEndDate()));
    }

}
