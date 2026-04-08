package ru.andrewb.charm.back.validator;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.dto.PasswordChangeDto;

@Component
public class PasswordChangeValidator implements Validator<PasswordChangeDto> {

    @Override
    public ValidationResult validate(PasswordChangeDto dto) {
        var vr = new ValidationResult();
        if (dto == null) {
            vr.addError("error.dto.required");
            return vr;
        }

        String currPwd = PasswordUtils.normalize(dto.getCurrentPassword());
        boolean hasCurrPwd = PasswordUtils.hasText(currPwd);
        if (!hasCurrPwd) {
            vr.addError("error.password.current-required");
        }

        String newPwd = PasswordUtils.normalize(dto.getNewPassword());
        boolean hasNewPwd = PasswordUtils.hasText(newPwd);
        if (!hasNewPwd) {
            vr.addError("error.password.new-required");
        } else if (newPwd.length() < 6) {
            vr.addError("error.password.short");
        }

        String confirmPwd = PasswordUtils.normalize(dto.getConfirmPassword());
        boolean hasConfirmPwd = PasswordUtils.hasText(confirmPwd);
        if (!hasConfirmPwd) {
            vr.addError("error.password.confirm-required");
        } else if (!confirmPwd.equals(newPwd)) {
            vr.addError("error.password.mismatch");
        }

        return vr;
    }
}

