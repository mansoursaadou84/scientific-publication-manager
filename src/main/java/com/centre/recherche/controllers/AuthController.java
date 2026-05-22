package com.centre.recherche.controllers;

import com.centre.recherche.models.Role;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.UserRepository;
import com.centre.recherche.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST d'authentification (préfixe : /api/auth).
 * Fournit les endpoints de connexion, d'inscription et de récupération
 * des informations de l'utilisateur connecté via JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /** Authentifie un utilisateur et retourne un token JWT. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = userRepository.findByUsername(request.getUsername());
            String token = jwtService.generateToken(
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("role", user.getRole().name());
            response.put("roles", roles);
            response.put("email", user.getEmail() != null ? user.getEmail() : "");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Identifiants invalides");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /** Inscrit un nouvel utilisateur et retourne un token JWT. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Nom d'utilisateur deja pris");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.CHERCHEUR);
        user.setEmail(request.getEmail());
        userRepository.save(user);

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Retourne les informations de l'utilisateur authentifié. */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findByUsername(authentication.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("email", user.getEmail() != null ? user.getEmail() : "");
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    // ── DTOs internes ────────────────────────────────────────────────────

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private Role role;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}