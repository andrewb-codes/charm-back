package ru.andrewb.charm.back.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import static ru.andrewb.charm.back.web.Urls.*;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                "/",
                                INDEX_URL,
                                LOGIN_URL,
                                REGISTRATION_URL,
                                LANG_URL,
                                "/img/**",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/fonts/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(ADMIN_URL + "/**").hasRole("ADMIN")
                        .requestMatchers(ADMIN_REST_PREFIX + "/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, resp, e) -> {
                            String uri = req.getRequestURI();
                            if (uri != null && uri.startsWith(req.getContextPath() + REST_PREFIX)) {
                                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            } else {
                                resp.sendRedirect(req.getContextPath() + LOGIN_URL);
                            }
                        })
                )
                .formLogin(form -> form
                        .loginPage(LOGIN_URL)
                        .loginProcessingUrl(LOGIN_URL)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl(INDEX_URL, true)
                        .failureUrl(LOGIN_URL + "?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl(LOGOUT_URL)
                        .logoutSuccessUrl(LOGIN_URL + "?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                )
                .csrf(Customizer.withDefaults());

        return http.build();
    }
}
