package ru.andrewb.charm.back.controller.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.andrewb.charm.back.model.Gender;

import java.time.LocalDate;

@Getter
@Setter
public class ProfileUpdateForm {
    private String name;
    private String surname;
    private String about;
    private LocalDate birthdate;
    private Gender gender;
    private Integer version;
    private MultipartFile photo;
}
