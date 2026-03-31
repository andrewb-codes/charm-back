package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.LoginDto;

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

        String email = EmailUtils.normalize(dto.getEmail());
        boolean hasEmail = EmailUtils.hasText(email);
        if (!hasEmail) {
            vr.addError("error.email.required");
        } else if (!EmailUtils.matchesFormat(email)) {
            vr.addError("error.email.invalid");
        }

        String pwd = PasswordUtils.normalize(dto.getPassword());
        boolean hasPwd = PasswordUtils.hasText(pwd);
        if (!hasPwd) {
            vr.addError("error.password.required");
        }

        return vr;
    }
}
