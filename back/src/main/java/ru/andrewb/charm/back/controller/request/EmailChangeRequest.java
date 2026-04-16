package ru.andrewb.charm.back.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailChangeRequest {

    @ToString.Include
    @NotBlank(message = "error.email.required")
    @Email(message = "error.email.invalid")
    String newEmail;

    @NotBlank(message = "error.password.current-required")
    String currentPassword;

    @NotNull(message = "error.param.required")
    Integer version;
}
