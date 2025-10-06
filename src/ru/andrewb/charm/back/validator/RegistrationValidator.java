package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.RegistrationDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationValidator implements Validator<RegistrationDto> {

    private final EmailValidator emailValidator = EmailValidator.getInstance();

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

        var emailVr = emailValidator.validate(dto.getEmail());
        vr.merge(emailVr);

        String pwd = dto.getPassword() == null ? null : dto.getPassword().trim();
        if (pwd == null || pwd.isBlank()) {
            vr.addError("error.password.required");
        } else if (pwd.length() < 6) {
            vr.addError("error.password.short");
        }

        return vr;
    }
}
