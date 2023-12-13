package services.traffic;

import checkUnits.CheckUnitType;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.CustomErdi;
import model.traffic.CustomErdiUnit;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.CustomErdiRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CreateCustomErdiService {

    private final CustomErdiRepository customErdiRepository;

    public Set<CustomErdi> createCustomErdiUnitsFromFile(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(this::createAndSaveCustomErdi)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new AS_15_8_PPT_Exception(String.format("Не удалось прочитать файл %s", file.getOriginalFilename()));
        }
    }


    private CustomErdi createAndSaveCustomErdi(String address) {
        CheckUnitType checkUnitType;
        try {
            checkUnitType = getCheckUnitType(address);
        } catch (URISyntaxException e) {
            log.warn("Не удалось определить CheckUnitType для ресурса {}. Ресурс не будет добавлен к мероприятию", address);
            return null;
        }
        CustomErdi customErdi = createCustomErdi(address, checkUnitType);
        try {
            return customErdiRepository.save(customErdi);
        } catch (Exception e) {
            log.warn("CustomErdi для ресурса {} уже существует", address);
            return null;
        }
    }

    private CustomErdi createCustomErdi(String address, CheckUnitType checkUnitType) {

        String customErdiName = address.startsWith("xn--") ? IDN.toUnicode(address) : address;
        CustomErdi customErdi = customErdiRepository.findByName(customErdiName)
                .orElse(new CustomErdi().setName(customErdiName));

        CustomErdiUnit customErdiUnit = new CustomErdiUnit()
                .setCustomErdi(customErdi)
                .setType(checkUnitType)
                .setValue(address);

        customErdi.addCustomErdiUnit(customErdiUnit);

        return customErdi;
    }


    private CheckUnitType getCheckUnitType(String address) throws URISyntaxException {
        UrlValidator urlValidator = new UrlValidator();
        if (urlValidator.isValid(address)) {
            return CheckUnitType.URL;
        } else {
            URI uri = new URI(address);
            return CheckUnitType.DOMAIN;
        }
    }

}
