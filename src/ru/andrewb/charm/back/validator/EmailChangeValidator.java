package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.EmailChangeDto;
import ru.andrewb.charm.back.utils.Emails;
import ru.andrewb.charm.back.utils.Passwords;

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

        String newEmail = Emails.normalize(dto.getNewEmail());
        boolean hasEmail = Emails.hasText(newEmail);
        if (!hasEmail) {
            vr.addError("error.email.required");
        } else if (!Emails.matchesFormat(newEmail)) {
            vr.addError("error.email.invalid");
        }

        String currPwd = Passwords.normalize(dto.getCurrentPassword());
        boolean hasCurrPwd = Passwords.hasText(currPwd);
        if (!hasCurrPwd) {
            vr.addError("error.email.password-required");
        }

        return vr;
    }
}
