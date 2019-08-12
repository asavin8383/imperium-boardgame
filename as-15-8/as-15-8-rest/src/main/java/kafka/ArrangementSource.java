package kafka;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface ArrangementSource {
    String OUTPUT = "arrangements-output";

    @Output(ArrangementSource.OUTPUT)
    MessageChannel output();
}
