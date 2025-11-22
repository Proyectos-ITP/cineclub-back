package com.cineclub_backend.cineclub_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String username;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    jwt = authHeader.substring(7);

    try {
      username = jwtService.extractUsername(jwt);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        if (jwtService.validateToken(jwt)) {
          List<SimpleGrantedAuthority> authorities = extractAuthorities(jwt);

          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            username,
            null,
            authorities
          );

          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      filterChain.doFilter(request, response);
    } catch (io.jsonwebtoken.ExpiredJwtException | io.jsonwebtoken.security.SignatureException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response
        .getWriter()
        .write(
          "{\"status\": 401, \"error\":\"No autorizado\",\"message\":\"El token es invalido o ha expirado.\"}"
        );
      return;
    } catch (io.jsonwebtoken.JwtException e) {
      logger.error("JWT error: " + e.getMessage(), e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response
        .getWriter()
        .write(
          "{\"status\": 401, \"error\":\"No autorizado\",\"message\":\"El token es invalido.\"}"
        );
      return;
    }
  }

  private List<SimpleGrantedAuthority> extractAuthorities(String token) {
    try {
      Object rolesClaim = jwtService.extractAllClaims(token).get("roles");

      if (rolesClaim instanceof List<?>) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) rolesClaim;
        return roles.stream().map(SimpleGrantedAuthority::new).toList();
      }

      return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    } catch (Exception e) {
      return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
  }
}
