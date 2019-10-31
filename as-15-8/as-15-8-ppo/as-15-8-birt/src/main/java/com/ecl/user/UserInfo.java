package com.ecl.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserInfo {

    @NonNull final String username;

    @JsonProperty(value = "first_name")
    private String firstName;
    @JsonProperty(value = "second_name")
    private String secondName;
}
