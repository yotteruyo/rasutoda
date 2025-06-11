package com.pelosa.rasutoda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                                "/api/parties/{partyId}/messages/file"
                        ).authenticated()

                        .requestMatchers(
                                "/mypage",
                                "/mypage/**",
                                "/party-create"
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
                .logout(logout->logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}