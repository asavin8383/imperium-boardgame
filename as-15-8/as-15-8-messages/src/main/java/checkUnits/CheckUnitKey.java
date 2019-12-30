package checkUnits;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUnitKey implements Comparable<CheckUnitKey> {
    private Long arrangementId;
    private Long jobId;

    @Override
    public int compareTo(CheckUnitKey o) {
        if(!this.arrangementId.equals(o.getArrangementId()))
            return this.arrangementId.compareTo(o.getArrangementId());
        else
            return this.jobId.compareTo(o.getJobId());
    }
}
