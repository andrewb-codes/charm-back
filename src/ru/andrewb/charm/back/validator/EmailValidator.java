package ru.andrewb.charm.back.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.utils.Emails;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailValidator implements Validator<String> {

    private static final EmailValidator INSTANCE = new EmailValidator();

    public static EmailValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidationResult validate(String rawEmail) {
        var vr = new ValidationResult();
        String email = Emails.normalize(rawEmail);

        if (!Emails.hasText(email)) {
            vr.addError("error.email.required");
            return vr;
        }
        if (!Emails.matchesFormat(email)) {
            vr.addError("error.email.invalid");
        }
        return vr;
    }
}
