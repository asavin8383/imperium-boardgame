package checkUnits;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckUnitKey implements Comparable<CheckUnitKey> {

    private Long arrangementId;

    private Long jobId;

    private Long version = 0L;

    public CheckUnitKey(Long arrangementId, Long jobId) {
        this.arrangementId = arrangementId;
        this.jobId = jobId;
    }

    @Override
    public int compareTo(CheckUnitKey o) {
        if(!this.arrangementId.equals(o.getArrangementId()))
            return this.arrangementId.compareTo(o.getArrangementId());
        else
            return this.jobId.compareTo(o.getJobId());
    }
}
