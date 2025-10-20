package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.PasswordChangeDto;
import ru.andrewb.charm.back.utils.Passwords;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordChangeValidator implements Validator<PasswordChangeDto> {

    private static final PasswordChangeValidator INSTANCE = new PasswordChangeValidator();

    public static PasswordChangeValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidationResult validate(PasswordChangeDto dto) {
        var vr = new ValidationResult();
        if (dto == null) {
            vr.addError("error.dto.required");
            return vr;
        }

        String currPwd = Passwords.normalize(dto.getCurrentPassword());
        boolean hasCurrPwd = Passwords.hasText(currPwd);
        if (!hasCurrPwd) {
            vr.addError("error.password.current-required");
        }

        String newPwd = Passwords.normalize(dto.getNewPassword());
        boolean hasNewPwd = Passwords.hasText(newPwd);
        if (!hasNewPwd) {
            vr.addError("error.password.new-required");
        } else if (newPwd.length() < 6) {
            vr.addError("error.password.short");
        }

        String confirmPwd = Passwords.normalize(dto.getConfirmPassword());
        boolean hasConfirmPwd = Passwords.hasText(confirmPwd);
        if (!hasConfirmPwd) {
            vr.addError("error.password.confirm-required");
        } else if (!confirmPwd.equals(newPwd)) {
            vr.addError("error.password.mismatch");
        }

        return vr;
    }
}

