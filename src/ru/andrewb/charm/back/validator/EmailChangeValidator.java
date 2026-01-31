package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.utils.EmailUtils;
import ru.andrewb.charm.back.utils.PasswordUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailChangeValidator implements Validator<EmailChangeDto> {

    private static final EmailChangeValidator INSTANCE = new EmailChangeValidator();

    public static EmailChangeValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidationResult validate(EmailChangeDto dto) {
        var vr = new ValidationResult();
        if (dto == null) {
            vr.addError("error.dto.required");
            return vr;
        }

        String newEmail = EmailUtils.normalize(dto.getNewEmail());
        boolean hasEmail = EmailUtils.hasText(newEmail);
        if (!hasEmail) {
            vr.addError("error.email.required");
        } else if (!EmailUtils.matchesFormat(newEmail)) {
            vr.addError("error.email.invalid");
        }

        String currPwd = PasswordUtils.normalize(dto.getCurrentPassword());
        boolean hasCurrPwd = PasswordUtils.hasText(currPwd);
        if (!hasCurrPwd) {
            vr.addError("error.password.current-required");
        }

        return vr;
    }
}
