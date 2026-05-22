package com.centre.recherche.services;

import com.centre.recherche.models.User;
import com.centre.recherche.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service de gestion des utilisateurs.
 * <p>
 * Fournit les operations CRUD de base sur les utilisateurs.
 * <strong>Note :</strong> l'encodage du mot de passe n'est PAS effectue ici ;
 * il est realise dans les controleurs (RegistrationController, AdminController)
 * avant l'appel a {@link #save(User)}.
 * </p>
 *
 * @see User
 * @see UserRepository
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /** Recherche un utilisateur par son nom d'utilisateur. */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Sauvegarde un utilisateur SANS encoder le mot de passe.
     * L'encodage est realise dans les controleurs avant l'appel.
     */
    public User save(User user) {
        // Sauvegarde directe SANS encoder le mot de passe
        // L'encodage est fait dans RegistrationController ou AdminController
        return userRepository.save(user);
    }

    /** Retourne la liste de tous les utilisateurs. */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /** Retourne un utilisateur par son identifiant, ou null si introuvable. */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /** Supprime un utilisateur par son identifiant. */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}