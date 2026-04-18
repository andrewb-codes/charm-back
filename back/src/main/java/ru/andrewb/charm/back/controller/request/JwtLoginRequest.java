package ru.andrewb.charm.back.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtLoginRequest {

    @NotBlank(message = "error.email.required")
    String email;

    @NotBlank(message = "error.password.required")
    String password;
}
