package enums;

/**
 * События, вызывающие переход между состояниями мероприятия
 * Creation date: 09.08.2019
 * Author: asavin
 */
public enum ArrangementEvents {
    FILL,
    SCHEDULE,
    SCHEDULE_ROLLBACK,
    RUN,
    FINISH,
    PREPARE_TO_STOP,
    STOP,
    SEND_ACT,
    STOP_BY_SERVICE_MODE,
    STOP_BY_MAX_CHECK_UNITS_COUNT,
    MANUAL_SCHEDULE
}
