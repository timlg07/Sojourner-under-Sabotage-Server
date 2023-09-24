package de.tim_greller.susserver.service.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class SusUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public SusUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userService
                .loadUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
