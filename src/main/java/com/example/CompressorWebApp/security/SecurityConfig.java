package com.example.CompressorWebApp.security;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
                        .requestMatchers("/registration", "/css/**", "/js/**", "/error", "/auth").permitAll()
                        .anyRequest().authenticated() // остальные защищены
                )
                .formLogin(form -> form
                        .loginPage("/auth") // путь к кастомной странице входа
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth?error")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .alwaysRemember(true)
                        .tokenValiditySeconds(43200)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")          // URL выхода
                        .logoutSuccessUrl("/auth")         // куда редиректить после выхода
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID" , "REMEMBERME")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                );


        return http.build();
    }

    @Bean
    public ServletContextInitializer cookieConfig() {
        return servletContext -> {
            servletContext.getSessionCookieConfig()
                    .setMaxAge(43200);
        };
    }


}