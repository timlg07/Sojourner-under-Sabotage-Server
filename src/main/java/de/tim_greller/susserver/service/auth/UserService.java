package de.tim_greller.susserver.service.auth;

import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

import de.tim_greller.susserver.dto.UserRegistrationDTO;
import de.tim_greller.susserver.exception.UserAlreadyExistException;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserEntity registerNewUserAccount(UserRegistrationDTO userDto) throws UserAlreadyExistException {
        if (usernameExists(userDto.getUsername())) {
            throw new UserAlreadyExistException("The user " + userDto.getUsername() + " already exists.");
        }

        var user = UserEntity.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .encodedPassword(encoder.encode(userDto.getPassword()))
                .username(userDto.getUsername())
                .build();
        return userRepository.save(user);
    }

    private boolean usernameExists(String username) {
        return userRepository.existsById(username);
    }

    public Optional<UserEntity> loadUserByUsername(String username) {
        return userRepository.findById(username);
    }

    public Object getPrincipal() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated() || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal().toString())) {
            return null;
        }

        return auth.getPrincipal();
    }

    public Optional<String> getCurrentUserId() {
        final Object principal = getPrincipal();
        if (principal == null) {
            return Optional.empty();
        }

        final String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof Principal p) {
            username = p.getName();
        } else {
            username = principal.toString();
        }
        return Optional.of(username);
    }

    public String requireCurrentUserId() {
        return getCurrentUserId().orElseThrow();
    }

    public UserEntity requireCurrentUser() {
        return loadUserByUsername(requireCurrentUserId()).orElseThrow();
    }

    /**
     * Overrides the principal returned by {@link #getPrincipal()}. This is useful for occasions where
     * the principal is not available in the SecurityContext, e.g., when handling STOMP messages.
     *
     * @param principal the principal to return from {@link #getPrincipal()}
     */
    public void overridePrincipal(Principal principal) {
        SecurityContextHolder.getContext().setAuthentication(new InternalPrincipalWrapper(principal));
    }

    @Getter
    @RequiredArgsConstructor
    @SuppressWarnings("ClassCanBeRecord") // no, thanks
    private static class InternalPrincipalWrapper implements Authentication {
        private final Principal principal;
        @Override public Collection<? extends GrantedAuthority> getAuthorities() { throw new UnsupportedOperationException(); }
        @Override public Object getCredentials() { throw new UnsupportedOperationException(); }
        @Override public Object getDetails() { throw new UnsupportedOperationException(); }
        @Override public boolean isAuthenticated() { return true; }
        @Override public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { throw new UnsupportedOperationException(); }
        @Override public String getName() { return principal.getName(); }
    }
}
