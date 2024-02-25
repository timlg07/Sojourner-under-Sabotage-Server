package de.tim_greller.susserver.service.auth;

import java.security.Principal;
import java.util.Optional;

import de.tim_greller.susserver.dto.UserRegistrationDTO;
import de.tim_greller.susserver.exception.UserAlreadyExistException;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private Object principal;

    public UserService(@Autowired UserRepository repository, @Autowired PasswordEncoder encoder) {
        this.userRepository = repository;
        this.encoder = encoder;
    }

    public UserEntity registerNewUserAccount(UserRegistrationDTO userDto) throws UserAlreadyExistException {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        UserEntity user = new UserEntity();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEncodedPassword(encoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());

        return userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return userRepository.existsById(email);
    }

    public Optional<UserEntity> loadUserByEmail(String username) {
        return userRepository.findById(username);
    }

    public Object getPrincipal() {
        if (this.principal != null) {
            return this.principal;
        }

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.isAuthenticated()) {
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

    /**
     * Overrides the principal returned by {@link #getPrincipal()}. This is useful for occasions where
     * the principal is not available in the SecurityContext, e.g., when handling STOMP messages.
     *
     * @param principal the principal to return from {@link #getPrincipal()}
     */
    public void overridePrincipal(Object principal) {
        this.principal = principal;
    }

    /**
     * Resets the principal returned by {@link #getPrincipal()} to the value from the SecurityContext.
     */
    public void resetPrincipal() {
        this.principal = null;
    }
}
