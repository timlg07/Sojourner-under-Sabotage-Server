package de.tim_greller.susserver.validation;

import de.tim_greller.susserver.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    public static final int MIN_LENGTH = 8;
    public static final boolean NEEDS_NUMERIC = true;
    public static final boolean NEEDS_ALPHABETIC = true;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        boolean longEnough = s.length() >= MIN_LENGTH;
        boolean hasNumeric = s.matches(".*\\d.*");
        boolean hasAlphabetic = s.matches(".*[a-zA-Z].*");
        return longEnough && (!NEEDS_NUMERIC || hasNumeric) && (!NEEDS_ALPHABETIC || hasAlphabetic);
    }
}
