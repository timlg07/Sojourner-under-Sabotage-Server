package de.tim_greller.susserver.securingweb;

import static org.springframework.security.config.Customizer.withDefaults;

import de.tim_greller.susserver.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
    @Configuration
    public static class WebSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChainWeb(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests((requests) -> requests
                            .requestMatchers("/", "/home", "/register").permitAll()
                            .anyRequest().authenticated()
                    )
                    .formLogin((form) -> form
                            .loginPage("/login")
                            .permitAll()
                    )
                    .logout(LogoutConfigurer::permitAll)
                    .build();
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @Configuration
    public static class ApiSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChainAPI(HttpSecurity http) throws Exception {
            return http
                    .securityMatcher("/api/**")
                    .cors(withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests((requests) -> requests
                            .requestMatchers("/api/hello").permitAll()
                            .requestMatchers("/api/**").authenticated()
                    )
                    .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/api/**", new CorsConfiguration().applyPermitDefaultValues());
            return source;
        }
    }


    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new SusUserDetailsService(userService);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}