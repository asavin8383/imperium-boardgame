package com.ecls.autoconfiguration.model;

import com.ecls.autoconfiguration.exceptions.AS_15_8_System_Mode_Exception;
import enums.SystemModeUnit;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CurrentSystemMode {

    private SystemModeUnit systemModeUnit;
    private String systemModeUrl;

    public CurrentSystemMode(String systemModeUrl) {
        this.systemModeUrl = systemModeUrl;
    }

    public synchronized SystemModeUnit getSystemModeUnit() {
        if (systemModeUnit == null) {
            return requestCurrentSystemMode();
        }
        return systemModeUnit;
    }

    public synchronized void setSystemModeUnit(SystemModeUnit systemModeUnit_) {
        systemModeUnit = systemModeUnit_;
    }


    private SystemModeUnit requestCurrentSystemMode() {
        try {
            ResponseEntity<SystemModeUnit> responseEntity = new RestTemplate()
                    .exchange(systemModeUrl,
                            HttpMethod.POST,
                            null,
                            SystemModeUnit.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                SystemModeUnit systemModeUnit = responseEntity.getBody();
                return systemModeUnit;
            } else {
                throw new AS_15_8_System_Mode_Exception("Ошибка запроса текущего режима работы с конфиг сервера, статус : " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            throw new AS_15_8_System_Mode_Exception("Ошибка запроса текущего режима работы с конфиг сервера :" + e);
        }

    }

}
