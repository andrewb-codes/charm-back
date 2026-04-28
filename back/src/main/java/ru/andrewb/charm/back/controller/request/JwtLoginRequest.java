package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Schema(description = "Login request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtLoginRequest {

    @Schema(description = "User email", example = "ivanov@mail.ru")
    @NotBlank(message = "error.email.required")
    String email;

    @Schema(description = "User password", example = "123456", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "error.password.required")
    String password;
}
