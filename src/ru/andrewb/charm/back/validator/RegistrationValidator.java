package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.RegistrationDto;
import ru.andrewb.charm.back.utils.Emails;
import ru.andrewb.charm.back.utils.Passwords;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationValidator implements Validator<RegistrationDto> {

    private static final RegistrationValidator INSTANCE = new RegistrationValidator();

    public static RegistrationValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidationResult validate(RegistrationDto dto) {
        var vr = new ValidationResult();

        if (dto == null) {
            vr.addError("error.dto.required");
            return vr;
        }

        String email = Emails.normalize(dto.getEmail());
        boolean hasEmail = Emails.hasText(email);
        if (!hasEmail) {
            vr.addError("error.email.required");
        } else if (!Emails.matchesFormat(email)) {
            vr.addError("error.email.invalid");
        }

        String pwd = Passwords.normalize(dto.getPassword());
        boolean hasPwd = Passwords.hasText(pwd);
        if (!hasPwd) {
            vr.addError("error.password.required");
        } else if (pwd.length() < 6) {
            vr.addError("error.password.short");
        }

        return vr;
    }
}
