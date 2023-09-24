package de.tim_greller.susserver.securingweb;

import static org.springframework.security.config.Customizer.withDefaults;

import de.tim_greller.susserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    /**
     * Apply the API filter chain first. Uses JWT tokens for authentication & authorization.
     * Creation of a new token is done by the {@link de.tim_greller.susserver.controller.api.ApiAuthenticationController}
     * via username and password. The {@link JwtRequestFilter} handles the authorization via tokens.
     */
    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public static class ApiSecurityConfig {

        private final String apiUrl;
        private final JwtRequestFilter jwtRequestFilter;

        @Autowired
        public ApiSecurityConfig(JwtRequestFilter jwtRequestFilter, @Value("${paths.api}") String apiUrl) {
            this.jwtRequestFilter = jwtRequestFilter;
            this.apiUrl = apiUrl;
        }

        @Bean
        public SecurityFilterChain securityFilterChainAPI(HttpSecurity http) throws Exception {
            return http
                    .securityMatcher(apiUrl + "/**")
                    .cors(withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests((requests) -> requests
                            .requestMatchers(apiUrl + "/hello", apiUrl + "/auth").permitAll()
                            .requestMatchers(apiUrl + "/**").authenticated()
                    )
                    .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration(apiUrl + "/**", new CorsConfiguration().applyPermitDefaultValues());
            return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                throws Exception {
            return authenticationConfiguration.getAuthenticationManager();
        }
    }

    /**
     * Filter applied to all URLs (except the API-URLs already handled by the higher precedence filter) and require
     * default Spring Security Authentication.
     */
    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE + 2)
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


    // Services used by API and Web

    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new SusUserDetailsService(userService);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}