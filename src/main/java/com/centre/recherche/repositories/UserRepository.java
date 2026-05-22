package com.centre.recherche.repositories;

import com.centre.recherche.models.Role;
import com.centre.recherche.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Depot JPA pour l'entite {@link User}.
 * Fournit les operations de base et des methodes de recherche
 * par nom d'utilisateur et par role.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur
     * @return l'utilisateur correspondant, ou null si aucun trouve
     */
    User findByUsername(String username);

    /**
     * Recherche les utilisateurs ayant un role donne.
     *
     * @param role le role a filtrer
     * @return la liste des utilisateurs correspondants
     */
    List<User> findByRole(Role role);
}
