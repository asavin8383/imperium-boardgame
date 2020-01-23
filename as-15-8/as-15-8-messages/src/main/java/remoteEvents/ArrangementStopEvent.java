package remoteEvents;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ArrangementStopEvent extends RemoteApplicationEvent {

    private Long arrangementId;
    private Long version;

    public ArrangementStopEvent(Object source, String originService, Long arrangementId, Long version) {
        super(source, originService);
        this.arrangementId = arrangementId;
        this.version = version;
    }
}
