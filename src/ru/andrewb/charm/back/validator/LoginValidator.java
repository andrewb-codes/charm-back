package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.LoginDto;
import ru.andrewb.charm.back.utils.Emails;
import ru.andrewb.charm.back.utils.Passwords;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginValidator implements Validator<LoginDto> {

    private static final LoginValidator INSTANCE = new LoginValidator();

    public static LoginValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidationResult validate(LoginDto dto) {
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
        }

        return vr;
    }
}
