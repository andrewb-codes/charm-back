package ru.andrewb.charm.back.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
import ru.andrewb.charm.back.model.Gender;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Profile update request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {

    @Schema(description = "Name", example = "Ivan")
    @Size(max = 100, message = "error.name.too-long")
    String name;

    @Schema(description = "Surname", example = "Ivanov")
    @Size(max = 100, message = "error.surname.too-long")
    String surname;

    @Schema(description = "Profile description", example = "I am a Java Dev")
    @Size(max = 1000, message = "error.about.too-long")
    String about;

    @Schema(description = "Birth date", example = "2001-12-03")
    @Past(message = "error.birthdate.future")
    LocalDate birthdate;

    @Schema(description = "Gender", example = "MALE")
    Gender gender;

    @Schema(description = "Optimistic locking version", example = "0")
    @NotNull(message = "error.param.required")
    Integer version;

    @Schema(hidden = true)
    MultipartFile photo;
}
