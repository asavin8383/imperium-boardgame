package checkUnits;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckUnitKey implements Comparable<CheckUnitKey> {

    @NonNull
    private Long arrangementId;

    @NonNull
    private Long jobId;

    private Long version = 0L;

    @Override
    public int compareTo(CheckUnitKey o) {
        if(!this.arrangementId.equals(o.getArrangementId()))
            return this.arrangementId.compareTo(o.getArrangementId());
        else
            return this.jobId.compareTo(o.getJobId());
    }
}
