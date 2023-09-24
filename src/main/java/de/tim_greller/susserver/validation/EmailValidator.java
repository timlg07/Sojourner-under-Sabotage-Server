package de.tim_greller.susserver.validation;

import java.util.regex.Pattern;

import de.tim_greller.susserver.validation.annotation.ValidEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-+]+(\\.[\\w-]+)*@"
            + "[\\w-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    @Override
    public void initialize(ValidEmail constraintAnnotation) {}

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
