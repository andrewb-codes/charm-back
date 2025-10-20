package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;

import java.time.LocalDate;
import java.time.Period;

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

        if (dto.getName() != null && dto.getName().length() > 100) {
            vr.addError("error.name.too-long");
        }
        if (dto.getSurname() != null && dto.getSurname().length() > 100) {
            vr.addError("error.surname.too-long");
        }
        if (dto.getAbout() != null && dto.getAbout().length() > 1000) {
            vr.addError("error.about.too-long");
        }

        if (dto.getBirthDate() != null) {
            LocalDate today = LocalDate.now();
            var bd = dto.getBirthDate();

            if (bd.isAfter(today)) {
                vr.addError("error.birthdate.future");
            }
            if (bd.isBefore(LocalDate.of(1900, 1, 1))) {
                vr.addError("error.birthdate.too-old");
            }

            int years = Period.between(bd, today).getYears();
            if (years < 18) {
                vr.addError("error.birthdate.underage");
            }
        }

        return vr;
    }
}
