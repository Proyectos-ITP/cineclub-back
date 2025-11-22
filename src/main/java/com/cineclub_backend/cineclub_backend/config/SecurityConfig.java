package com.cineclub_backend.cineclub_backend.config;

import com.cineclub_backend.cineclub_backend.security.JwtAuthenticationEntryPoint;
import com.cineclub_backend.cineclub_backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${docs.username}")
  private String docsUsername;

  @Value("${docs.password}")
  private String docsPassword;

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

  public SecurityConfig(
    JwtAuthenticationFilter jwtAuthFilter,
    JwtAuthenticationEntryPoint jwtAuthEntryPoint
  ) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.jwtAuthEntryPoint = jwtAuthEntryPoint;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user = User.builder()
      .username(docsUsername)
      .password(passwordEncoder().encode(docsPassword))
      .roles("DOCS")
      .build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  @Order(1)
  public SecurityFilterChain docsSecurityFilterChain(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/docs", "/api-docs.html")
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
      .httpBasic(basic -> {})
      .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/**")
      .cors(cors -> cors.configure(http))
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            "/",
            "/health",
            "/actuator/health",
            "/ws/**",
            "/v3/api-docs/**",
            "/diagram.svg"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
