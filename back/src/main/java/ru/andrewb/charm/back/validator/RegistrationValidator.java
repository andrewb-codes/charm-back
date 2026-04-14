package ru.andrewb.charm.back.validator;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.dto.RegistrationDto;

@Component
public class RegistrationValidator implements Validator<RegistrationDto> {

    @Override
    public ValidationResult validate(RegistrationDto dto) {
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
        } else if (pwd.length() < 6) {
            vr.addError("error.password.short");
        }

        return vr;
    }
}
