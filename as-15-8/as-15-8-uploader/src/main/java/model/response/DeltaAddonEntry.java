package model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User: asinjavin
 * Date: 18.10.2019
 * Time: 20:07
 */
@Data
@AllArgsConstructor
public class DeltaAddonEntry
{
    long deltaId;
    LocalDateTime actualDate;
    boolean isEmpty;
}
