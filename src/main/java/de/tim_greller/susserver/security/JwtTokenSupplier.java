package de.tim_greller.susserver.security;

import java.util.Optional;

import de.tim_greller.susserver.service.auth.JwtTokenService;
import de.tim_greller.susserver.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenSupplier {

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Autowired
    public JwtTokenSupplier(JwtTokenService jwtTokenService, UserService userService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    public Optional<String> getJwtToken() {
        return userService.getCurrentUserId().map(jwtTokenService::generateToken);
    }
}
