package de.tim_greller.susserver.controller.api;

import de.tim_greller.susserver.service.auth.JwtTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ApiAuthenticationController {

    final UserDetailsService jwtUserDetailsService;
    final JwtTokenService jwtTokenService;
    final AuthenticationManager authenticationManager;

    @Autowired
    public ApiAuthenticationController(UserDetailsService jwtUserDetailsService, JwtTokenService jwtTokenService,
                                       AuthenticationManager authenticationManager) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("${paths.api}/auth")
    public AuthenticationResponse authenticate(@RequestBody @Valid final AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getLogin(), authenticationRequest.getPassword()));
        } catch (final BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(authenticationRequest.getLogin());
        final AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAccessToken(jwtTokenService.generateToken(userDetails));
        return authenticationResponse;
    }

    @Getter
    @Setter
    public static class AuthenticationRequest {

        @NotNull
        @Size(max = 255)
        private String login;

        @NotNull
        @Size(max = 255)
        private String password;

    }

    @Setter
    @Getter
    public static class AuthenticationResponse {

        private String accessToken;

    }
}
