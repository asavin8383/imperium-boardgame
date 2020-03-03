package stateMachine;

import enums.ArrangementEvents;
import exceptions.AS_15_8_PPT_Exception;
import enums.ExecutionStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

import java.util.EnumSet;

/**
 * Конфигурация конечного автомата для смены состояний мероприятия
 * Creation date: 09.08.2019
 * Author: asavin
 */
public class ArrangementStateMachine {

    private final StateMachine<ExecutionStatus, ArrangementEvents> stateMachine;

    public ArrangementStateMachine(ExecutionStatus initialStatus){
        this.stateMachine = buildMachine(initialStatus);
        this.stateMachine.start();
    }

    public boolean sendEvent(ArrangementEvents event){
        return this.stateMachine.sendEvent(event);
    }

    public ExecutionStatus getCurrentStatus(){
        return this.stateMachine.getState().getId();
    }

    private StateMachine<ExecutionStatus, ArrangementEvents> buildMachine(ExecutionStatus initialStatus) {
        StateMachineBuilder.Builder<ExecutionStatus, ArrangementEvents> builder = StateMachineBuilder.builder();

        try {
            builder.configureStates()
                    .withStates()
                    .initial(initialStatus)
                    .states(EnumSet.allOf(ExecutionStatus.class));
            builder.configureTransitions()
                    .withExternal()
                    .source(ExecutionStatus.NEW).target(ExecutionStatus.FORMED)
                    .event(ArrangementEvents.FILL)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.FORMED).target(ExecutionStatus.SCHEDULED)
                    .event(ArrangementEvents.SCHEDULE)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPED).target(ExecutionStatus.SCHEDULED)
                    .event(ArrangementEvents.SCHEDULE)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.FORMED).target(ExecutionStatus.FORMED)
                    .event(ArrangementEvents.FILL)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.SCHEDULED).target(ExecutionStatus.RUNNING)
                    .event(ArrangementEvents.RUN)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.STOPPING)
                    .event(ArrangementEvents.PREPARE_TO_STOP)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.STOPPED)
                    .event(ArrangementEvents.STOP)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPING).target(ExecutionStatus.STOPPED)
                    .event(ArrangementEvents.STOP)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPING).target(ExecutionStatus.STOPPED_BY_MAX_CHECK_UNITS)
                    .event(ArrangementEvents.STOP)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPING).target(ExecutionStatus.STOPPED_BY_SERVICE_MODE)
                    .event(ArrangementEvents.STOP)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.NEW).target(ExecutionStatus.FINISHED)
                    .event(ArrangementEvents.STOP)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.SCHEDULED).target(ExecutionStatus.FORMED)
                    .event(ArrangementEvents.SCHEDULE_ROLLBACK)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.RUNNING)
                    .event(ArrangementEvents.RUN)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.FORMED)
                    .event(ArrangementEvents.SCHEDULE_ROLLBACK)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.FORMED).target(ExecutionStatus.FINISHED)
                    .event(ArrangementEvents.FINISH)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.SCHEDULED).target(ExecutionStatus.FINISHED)
                    .event(ArrangementEvents.FINISH)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.FINISHED)
                    .event(ArrangementEvents.FINISH)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPED).target(ExecutionStatus.FINISHED)
                    .event(ArrangementEvents.FINISH)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPED).target(ExecutionStatus.ACT_SENT)
                    .event(ArrangementEvents.SEND_ACT)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.FINISHED).target(ExecutionStatus.ACT_SENT)
                    .event(ArrangementEvents.SEND_ACT)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.ACT_SENT).target(ExecutionStatus.ACT_SENT)
                    .event(ArrangementEvents.SEND_ACT)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPED_BY_SERVICE_MODE).target(ExecutionStatus.ACT_SENT)
                    .event(ArrangementEvents.SEND_ACT)

                    .and()
                    .withExternal()
                    .source(ExecutionStatus.STOPPED_BY_MAX_CHECK_UNITS).target(ExecutionStatus.ACT_SENT)
                    .event(ArrangementEvents.SEND_ACT)
            ;

        } catch (Exception ex) {
            throw new AS_15_8_PPT_Exception("Ошибка создания конечного автомата!", ex);
        }

        return builder.build();
    }

}
