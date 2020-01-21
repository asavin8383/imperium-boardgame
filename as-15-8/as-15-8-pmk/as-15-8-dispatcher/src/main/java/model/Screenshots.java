package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Screenshots {

    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;

    /**Скриншот полученной при проверке страницы с выделенного прокси (эталона) */
    private byte[] etalonScreenshot;
}
