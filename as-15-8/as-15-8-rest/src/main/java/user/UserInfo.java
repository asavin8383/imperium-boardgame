package user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@NonNull private final String username;
	 
    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "second_name")
    private String secondName;

}
