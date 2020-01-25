package remoteEvents;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArrangementStopEvent {

    private Long arrangementId;
    private Long version;

    public ArrangementStopEvent(Long arrangementId, Long version) {
        this.arrangementId = arrangementId;
        this.version = version;
    }
}
