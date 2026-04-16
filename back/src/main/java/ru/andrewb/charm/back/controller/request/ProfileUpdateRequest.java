package ru.andrewb.charm.back.controller.request;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {

    @Size(max = 100, message = "error.name.too-long")
    String name;

    @Size(max = 100, message = "error.surname.too-long")
    String surname;

    @Size(max = 1000, message = "error.about.too-long")
    String about;

    @Past(message = "error.birthdate.future")
    LocalDate birthdate;

    Gender gender;

    @NotNull(message = "error.param.required")
    Integer version;

    MultipartFile photo;
}
