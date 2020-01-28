package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateErdiState {
    public Boolean isLoading = false;
    public String details = "";
    public String error = "";
}
