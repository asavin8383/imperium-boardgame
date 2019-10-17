package robots.factory.impl;

import enums.AccessToolParameter;
import enums.AccessToolUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import robots.Robot;
import common.ExecutorProperties;
import robots.factory.RobotsFactory;
import robots.impl.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RobotsFactoryImpl implements RobotsFactory {

    private final ExecutorProperties robotsProperties;

    private Map<AccessToolUnit, Class<? extends Robot>> scriptClasses = new HashMap<AccessToolUnit, Class<? extends Robot>>(){{
        put(AccessToolUnit.SEARCH_SYSTEM, CommonDirectSearchRobot.class);
        put(AccessToolUnit.PROXY, VPNRobot.class);
        put(AccessToolUnit.VPN, VPNRobot.class);
        put(AccessToolUnit.HOLA, HolaRobot.class);
        put(AccessToolUnit.CAMELEO_XYZ, CameleoRobot.class);
        put(AccessToolUnit.HIDEMYASS, HideMyAssRobot.class);
        put(AccessToolUnit.GOOGLE_API, GoogleApiRobot.class);
    }};

    public Robot createRobot(AccessToolUnit accessToolUnit, String robotName){
        Class<? extends Robot> scriptClass = scriptClasses.get(accessToolUnit);
        Map<AccessToolParameter, String> robotProps = robotsProperties.getProps()
                .getAccessToolUnits().get(accessToolUnit)
                .getRobotProps().get(robotName).getProps();
        return createRobotInstance(scriptClass, robotProps);
    }

    private Robot createRobotInstance(Class<? extends Robot> scriptClass, Object ...robotArgs) {
        try {
            return (Robot)scriptClass
                    .getConstructors()[0]
                    .newInstance(robotArgs);
        } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Ошибка при создании скрипта робота", ex);
        }
    }
}
