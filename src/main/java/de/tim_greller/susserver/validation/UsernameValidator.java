package de.tim_greller.susserver.validation;

import java.util.regex.Pattern;

import de.tim_greller.susserver.validation.annotation.ValidUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^\\w{3,}$");

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context){
        return USERNAME_PATTERN.matcher(username).matches();
    }
}
