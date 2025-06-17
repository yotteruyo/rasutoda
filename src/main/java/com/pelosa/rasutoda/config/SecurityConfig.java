package com.pelosa.rasutoda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/parties/create",
                                "/api/parties/{partyId}/messages",
                                "/api/parties/{partyId}/messages/file",
                                "/api/parties/{partyId}/disband",
                                "/api/parties/{partyId}/leave"
                        ).authenticated()

                        .requestMatchers("/register/oauth2-additional-info").permitAll()
                        .requestMatchers("/api/register/oauth2-complete").permitAll()

                        .requestMatchers(
                                "/mypage",
                                "/mypage/**",
                                "/party-create",
                                "/api/contact"
                        ).authenticated()

                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/api/users/register",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/terms",
                                "/privacy",
                                "/support",
                                "/faq",
                                "/party-list",
                                "/party/join/**",
                                "/payment/success",
                                "/payment/fail",
                                "/confirm"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin(form->form
                        .loginPage("/login")
                        .defaultSuccessUrl("/mypage", true)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                                .loginPage("/login")
                                .successHandler(oauth2AuthenticationSuccessHandler())
                                .failureUrl("/login?error")
                )
                .logout(logout->logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                org.springframework.security.core.Authentication authentication) throws IOException, ServletException {
                Boolean needsAdditionalInfo = (Boolean) request.getSession().getAttribute("needs_additional_oauth2_info");

                if (needsAdditionalInfo != null && needsAdditionalInfo) {
                    response.sendRedirect("/register/oauth2-additional-info");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }}