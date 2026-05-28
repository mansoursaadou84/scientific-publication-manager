package com.centre.recherche.config;

import com.centre.recherche.models.Role;
import com.centre.recherche.models.User;
import com.centre.recherche.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise les utilisateurs par defaut au demarrage de l'application.
 * Cree les comptes de test ou reinitialise leur mot de passe si necessaire.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureUser("admin", "admin123", Role.ADMIN, "admin@centre-recherche.ma");
        ensureUser("chercheur1", "1234", Role.CHERCHEUR, "chercheur1@centre-recherche.ma");
        ensureUser("chercheur2", "1234", Role.CHERCHEUR, "chercheur2@centre-recherche.ma");
        ensureUser("documentaliste", "1234", Role.DOCUMENTALISTE, "doc@centre-recherche.ma");
    }

    private void ensureUser(String username, String rawPassword, Role role, String email) {
        User existing = userRepository.findByUsername(username);
        if (existing == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEmail(email);
            userRepository.save(user);
            log.info("Utilisateur cree : {} (role={})", username, role);
        } else {
            existing.setPassword(passwordEncoder.encode(rawPassword));
            existing.setRole(role);
            existing.setEmail(email);
            userRepository.save(existing);
            log.info("Utilisateur reinitialise : {} (role={})", username, role);
        }
    }
}