package com.centre.recherche.models;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Entite JPA representant un chercheur, profil etendu lie a un {@link User}.
 *
 * <p>Un chercheur possede des informations academiques (specialite, info academique),
 * une photo de profil, et est lie a ses publications via une relation ManyToMany.</p>
 */
@Entity
public class Researcher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String specialty;
    private String academicInfo;
    private String photoUrl;

    @OneToOne
    private User user;

    @ManyToMany(mappedBy = "authors")
    private List<Publication> publications;

    public Researcher() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getAcademicInfo() { return academicInfo; }
    public void setAcademicInfo(String academicInfo) { this.academicInfo = academicInfo; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<Publication> getPublications() { return publications; }
    public void setPublications(List<Publication> publications) { this.publications = publications; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Researcher)) return false;
        Researcher that = (Researcher) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
