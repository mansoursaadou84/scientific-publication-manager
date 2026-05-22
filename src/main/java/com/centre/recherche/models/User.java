package com.centre.recherche.models;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entite JPA representant un utilisateur du systeme.
 *
 * <p>Chaque utilisateur possede un identifiant, un mot de passe encode (BCrypt),
 * un {@link Role} determinant ses permissions, et optionnellement un email.</p>
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String email;

    // Constructeurs
    public User() {}

    // Getters et Setters (Les outils pour lire et écrire les données)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
