package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Registration request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrationRequest {

    @ToString.Include
    @Schema(description = "User email", example = "new-user@mail.com")
    @NotBlank(message = "error.email.required")
    @Email(message = "error.email.invalid")
    String email;

    @Schema(description = "User password, at least 6 characters", example = "123456", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "error.password.required")
    @Size(min = 6, message = "error.password.short")
    String password;
}
