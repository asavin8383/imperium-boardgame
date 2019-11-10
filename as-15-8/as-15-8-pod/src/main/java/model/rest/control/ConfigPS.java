package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigPS
{
    Long id;
    String name;
    String hostname;
}
