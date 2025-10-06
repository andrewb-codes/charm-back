package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.model.Status;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileUpdateValidator implements Validator<ProfileUpdateDto> {

    private static final ProfileUpdateValidator INSTANCE = new ProfileUpdateValidator();

    public static ProfileUpdateValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidationResult validate(ProfileUpdateDto dto) {
        var vr = new ValidationResult();

        if (dto == null) {
            vr.addError("error.dto.required");
            return vr;
        }

        if (dto.getStatus() == Status.ACTIVE) {
            if (dto.getName() == null) vr.addError("error.name.required");
            if (dto.getSurname() == null) vr.addError("error.surname.required");
            if (dto.getGender() == null) vr.addError("error.gender.required");
            if (dto.getBirthDate() == null) vr.addError("error.birthdate.required");

        }

        if (dto.getName() != null && dto.getName().length() > 100) {
            vr.addError("error.name.tooLong");
        }
        if (dto.getSurname() != null && dto.getSurname().length() > 100) {
            vr.addError("error.surname.tooLong");
        }
        if (dto.getAbout() != null && dto.getAbout().length() > 1000) {
            vr.addError("error.about.tooLong");
        }
        if (dto.getBirthDate() != null) {
            if (dto.getBirthDate().isAfter(LocalDate.now())) {
                vr.addError("error.birthdate.future");
            }
            if (dto.getBirthDate().isBefore(LocalDate.of(1900, 1, 1))) {
                vr.addError("error.birthdate.tooOld");
            }
        }
        return vr;
    }
}
