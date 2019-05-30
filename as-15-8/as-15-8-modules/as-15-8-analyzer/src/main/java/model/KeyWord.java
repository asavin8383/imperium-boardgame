package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *  Ключевое слово
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyWord implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String word;
    private String type;
}