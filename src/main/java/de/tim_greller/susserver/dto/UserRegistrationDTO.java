package de.tim_greller.susserver.dto;

import de.tim_greller.susserver.validation.annotation.ValidPassword;
import de.tim_greller.susserver.validation.annotation.ValidUsername;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {

    @Setter
    private String firstName;

    @Setter
    private String lastName;

    @NotNull @NotEmpty @ValidPassword
    @Setter(onParam = @__({@NotNull, @NotEmpty, @ValidPassword}))
    private String password;

    @NotNull @NotEmpty @ValidUsername
    @Setter(onParam = @__({@NotNull, @NotEmpty, @ValidUsername}))
    private String username;
}
