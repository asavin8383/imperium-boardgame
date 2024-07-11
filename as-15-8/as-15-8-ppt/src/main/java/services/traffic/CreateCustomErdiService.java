package services.traffic;

import checkUnits.CheckUnitType;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.CustomErdi;
import model.traffic.CustomErdiUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.CustomErdiRepository;
import repositories.CustomErdiUnitRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CreateCustomErdiService {

    private final CustomErdiRepository customErdiRepository;
    private final CustomErdiUnitRepository customErdiUnitRepository;

    public Set<CustomErdi> createCustomErdiUnitsFromFile1(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(this::createOrGetCustomErdi)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new AS_15_8_PPT_Exception(String.format("Не удалось прочитать файл %s", file.getOriginalFilename()));
        }
    }

    public Set<CustomErdi> createCustomErdiUnitsFromFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            Set<String> addresses = reader.lines()
                    .map(address -> address.toLowerCase().trim())
                    .collect(Collectors.toSet());

            Set<Set<String>> addressesPackages = splitSet(addresses);
            Set<CustomErdi> result = new HashSet<>();
            for (Set<String> addressPackage : addressesPackages) {
                result.addAll(this.createOrGetCustomErdis(addressPackage));
            }
            return result;

        } catch (IOException e) {
            throw new AS_15_8_PPT_Exception(String.format("Не удалось прочитать файл %s", file.getOriginalFilename()));
        }
    }

    private Set<Set<String>> splitSet(Set<String> originalSet) {
        Set<Set<String>> result = new HashSet<>();
        Set<String> subSet = new HashSet<>();
        int count = 0;

        for (String element : originalSet) {
            subSet.add(element);
            count++;

            if (count == 30000) {
                result.add(subSet);
                subSet = new HashSet<>();
                count = 0;
            }
        }

        if (!subSet.isEmpty()) {
            result.add(subSet);
        }

        return result;
    }

    private Set<CustomErdi> createOrGetCustomErdis(Set<String> addresses) {
        Set<CustomErdi> existsCustomErdiWithCustomErdiUnitsValues = customErdiRepository.findAllByCustomErdiUnitsValuesIn(addresses);
        Set<String> existsCustomErdiUnitsValues = customErdiUnitRepository.findValuesByValueIn(addresses);
        Set<String> notFoundedInCustomErdiUnits = addresses.stream()
                .filter(address -> !existsCustomErdiUnitsValues.contains(address))
                .collect(Collectors.toSet());

        Set<CustomErdi> existsCustomErdis = customErdiRepository.findAllByNameIn(notFoundedInCustomErdiUnits);
        Set<String> existsCustomErdisNames = customErdiRepository.findNamesByNameIn(notFoundedInCustomErdiUnits);
        Set<String> notFoundedInCustomErdisAndCustomErdiUnits = notFoundedInCustomErdiUnits.stream()
                .filter(address -> !existsCustomErdisNames.contains(address))
                .collect(Collectors.toSet());

        Set<CustomErdi> createdCustomErdi = notFoundedInCustomErdisAndCustomErdiUnits.stream()
                .map(address -> {
                    String customErdiName = address.startsWith("xn--") ? IDN.toUnicode(address) : address;
                    CheckUnitType checkUnitType = getCheckUnitType(customErdiName);
                    CustomErdi customErdi = new CustomErdi()
                            .setName(customErdiName);

                    CustomErdiUnit customErdiUnit = new CustomErdiUnit()
                            .setCustomErdi(customErdi)
                            .setType(checkUnitType)
                            .setValue(address);

                    customErdi.addCustomErdiUnit(customErdiUnit);

                    return customErdi;
                })
                .collect(Collectors.toSet());

        List<CustomErdi> savedCustomErdis = customErdiRepository.saveAll(createdCustomErdi);

        Set<CustomErdi> resultCustomErdis = new HashSet<>(savedCustomErdis);
        resultCustomErdis.addAll(existsCustomErdis);
        resultCustomErdis.addAll(existsCustomErdiWithCustomErdiUnitsValues);
        return resultCustomErdis;
    }

    private CustomErdi createOrGetCustomErdi(String address) {
        address = address.toLowerCase().trim();

        Optional<CustomErdiUnit> customErdiUnitOptional = customErdiUnitRepository.findByValue(address);

        if (customErdiUnitOptional.isPresent()) {
            return customErdiUnitOptional.get().getCustomErdi();
        } else {
            CheckUnitType checkUnitType;
//            try {
//                checkUnitType = getCheckUnitType(address);
//            } catch (URISyntaxException e) {
//                log.warn("Не удалось определить CheckUnitType для ресурса {}. Ресурс не будет добавлен к мероприятию", address);
//                return null;
//            }
            String customErdiName = address.startsWith("xn--") ? IDN.toUnicode(address) : address;
            checkUnitType = getCheckUnitType(customErdiName);
            CustomErdi customErdi = createCustomErdi(address, customErdiName, checkUnitType);
            try {
                return customErdiRepository.save(customErdi);
            } catch (Exception e) {
                log.warn("Не удалось создать CustomErdi для ресурса {}. Причина: {}", address, e.getCause().getCause().getMessage());
                return null;
            }
        }
    }

    private CustomErdi createCustomErdi(String address, String customErdiName, CheckUnitType checkUnitType) {

        CustomErdi customErdi = customErdiRepository.findByName(customErdiName)
                .orElse(new CustomErdi().setName(customErdiName));

        CustomErdiUnit customErdiUnit = new CustomErdiUnit()
                .setCustomErdi(customErdi)
                .setType(checkUnitType)
                .setValue(address);

        customErdi.addCustomErdiUnit(customErdiUnit);

        return customErdi;
    }


    private CheckUnitType getCheckUnitType(String customErdiName) {

        if (StringUtils.startsWithAny(customErdiName, "http://", "https://")) {
            return CheckUnitType.URL;
        } else {
            return CheckUnitType.DOMAIN;
        }

//        UrlValidator urlValidator = new UrlValidator();
//        if (urlValidator.isValid(address)) {
//            return CheckUnitType.URL;
//        } else {
//            URI uri = new URI(address);
//            return CheckUnitType.DOMAIN;
//        }
    }

}
