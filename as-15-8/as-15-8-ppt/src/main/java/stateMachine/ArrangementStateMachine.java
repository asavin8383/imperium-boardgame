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
                    .source(ExecutionStatus.NEW).target(ExecutionStatus.ERROR)
                    .event(ArrangementEvents.FAIL)
                    .and()
                    .withExternal()
                    .source(ExecutionStatus.FORMED).target(ExecutionStatus.SCHEDULED)
                    .event(ArrangementEvents.SCHEDULE)
                    .and()
                    .withExternal()
                    .source(ExecutionStatus.SCHEDULED).target(ExecutionStatus.RUNNING)
                    .event(ArrangementEvents.RUN)
                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.ACTION_REQUIRED)
                    .event(ArrangementEvents.PAUSE)
                    .and()
                    .withExternal()
                    .source(ExecutionStatus.ACTION_REQUIRED).target(ExecutionStatus.RUNNING)
                    .event(ArrangementEvents.RESTORE)
                    .and()
                    .withExternal()
                    .source(ExecutionStatus.RUNNING).target(ExecutionStatus.FINISHED)
                    .event(ArrangementEvents.FINISH)
            ;
        } catch (Exception ex) {
            throw new AS_15_8_PPT_Exception("Ошибка создания конечного автомата!", ex);
        }

        return builder.build();
    }



}
