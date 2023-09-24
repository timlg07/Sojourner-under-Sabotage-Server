package de.tim_greller.susserver.dto;

import de.tim_greller.susserver.validation.annotation.ValidEmail;
import de.tim_greller.susserver.validation.annotation.ValidPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class UserRegistrationDTO {

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @NotNull @NotEmpty @ValidPassword
    @Setter(onParam = @__({@NotNull, @NotEmpty, @ValidPassword}))
    private String password;

    @NotNull @NotEmpty @ValidEmail
    @Setter(onParam = @__({@NotNull, @NotEmpty, @ValidEmail}))
    private String email;
}
