package ru.andrewb.charm.back.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrationRequest {

    @ToString.Include
    @NotBlank(message = "error.email.required")
    @Email(message = "error.email.invalid")
    String email;

    @NotBlank(message = "error.password.required")
    @Size(min = 6, message = "error.password.short")
    String password;
}
