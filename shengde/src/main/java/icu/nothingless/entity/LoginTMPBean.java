package icu.nothingless.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginTMPBean(
                @JsonProperty("username") String username, @JsonProperty("password") String password) {
}
