package com.centre.recherche.security;

import com.centre.recherche.models.User;
import com.centre.recherche.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;   // ← import manquant ajouté

/**
 * Implementation de {@link UserDetailsService} chargeant les utilisateurs
 * depuis la base de donnees via {@link UserRepository}. Les autorites
 * sont prefixees par "ROLE_" conformement aux conventions Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Charge un utilisateur par son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur a rechercher
     * @return les details de l'utilisateur authentifie
     * @throws UsernameNotFoundException si aucun utilisateur n'est trouve
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Utilisateur non trouvé : " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                getAuthorities(user.getRole().name())
        );
    }

    /**
     * Convertit un role en collection d'autorites Spring Security avec le prefixe ROLE_.
     *
     * @param role le nom du role (ex. ADMIN, CHERCHEUR)
     * @return la collection d'autorites
     */
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}