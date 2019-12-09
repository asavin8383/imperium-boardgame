package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigPS
{
    private Long origId;
    private String name;
    private String hostname;
}
