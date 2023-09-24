package de.tim_greller.susserver.security;

import java.util.Optional;

import de.tim_greller.susserver.service.auth.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenSupplier {

    private final JwtTokenService jwtTokenService;

    public JwtTokenSupplier(@Autowired JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public Optional<String> getJwtToken() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()) {
            return Optional.empty();
        }

        final Object principal = auth.getPrincipal();
        final String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return Optional.of(jwtTokenService.generateToken(username));
    }
}
