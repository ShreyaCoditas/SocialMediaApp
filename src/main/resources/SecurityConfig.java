package com.example.UserModeratorSystem.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return  http
                .csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/register", "/api/auth/login","/swagger-ui/**").permitAll()
                        // Moderator request — only logged-in USERs can request
                        .requestMatchers("/api/moderator/request-access/**").hasRole("USER")

                        // Posts & comments creation — logged-in users only
                        .requestMatchers("/api/publish/**").hasAnyRole("USER", "MODERATOR", "SUPER_ADMIN")

                        // Moderation APIs — only MODERATOR and SUPER_ADMIN
                        .requestMatchers("/api/moderation/**").hasAnyRole("MODERATOR", "SUPER_ADMIN")

                        // Admin actions — only SUPER_ADMIN
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                        // everything else needs authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }


//    {
//        "username": "shreya",
//            "email": "shreya@gmail.com",
//            "password": "Shreya@123"
//    }

//    {
//        "username": "Varun",
//            "email": "varun@gmail.com",
//            "password": "Varun@123"
//    }

//    {
//        "username": "superadmin",
//            "email": "superadmin@gmail.com",
//            "password": "Super@123"
//    }

//    {
//
//        "email": "vaneesha@gmail.com",
//            "password": "Vaneesha@123"
//    }




}





