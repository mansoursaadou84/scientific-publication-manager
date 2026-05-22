# Scientific Publication Manager

Application web de gestion des ressources documentaires et des publications
scientifiques pour un centre de recherche.

## Fonctionnalites

- Catalogue public avec recherche avancee (TF-IDF) et filtres (type, categorie, annee)
- Espace chercheur : soumission, modification, suivi des publications, profil, export PDF/Excel
- Espace documentaliste : validation et gestion du workflow editorial
- Espace administrateur : gestion des utilisateurs, categories, mots-cles, workflow
- Workflow de validation : Brouillon -> Soumise -> En validation -> Approuvee -> Publiee
- Export PDF et Excel des rapports et statistiques
- Notifications par email lors des changements de statut
- API REST securisee par JWT avec documentation Swagger/OpenAPI

## Technologies

| Composant           | Technologie                           |
|---------------------|---------------------------------------|
| Backend             | Spring Boot 3.2.5, Java 17           |
| Securite            | Spring Security, JWT (jjwt 0.11.5)   |
| Base de donnees      | MySQL 8, Spring Data JPA              |
| Moteur de templates | Thymeleaf + Layout Dialect            |
| Recherche           | TF-IDF (implementation personnalisee)|
| Export PDF          | iText 7                               |
| Export Excel         | Apache POI 5.2.5                      |
| Documentation API   | SpringDoc OpenAPI 2.3.0               |
| Email               | Spring Mail (SMTP)                    |
| Conteneurisation    | Docker, Docker Compose                |

## Prerequis

- Java 17+
- Maven 3.9+
- MySQL 8+
- Docker & Docker Compose (pour le deploiement conteneurise)

## Installation et demarrage

### 1. Cloner le depot

```bash
git clone <url-du-depot>
cd scientific-publication-manager
```

### 2. Configurer l'application

Copier le fichier exemple et renseigner vos identifiants :

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Editer `application.properties` avec vos valeurs :
- Mot de passe MySQL
- Identifiants SMTP pour l'envoi d'emails
- Cle secrete JWT (minimum 256 bits)

### 3. Creer la base de donnees

```sql
CREATE DATABASE scientific_publication_db;
```

### 4. Lancer l'application

```bash
mvn spring-boot:run
```

L'application est accessible sur [http://localhost:8080](http://localhost:8080).

## Deploiement avec Docker

1. Copier le fichier exemple et configurer les variables :

```bash
cp .env.example .env
# Editer .env avec vos identifiants
```

2. Lancer les conteneurs :

```bash
docker compose up --build -d
```

L'application est accessible sur [http://localhost:8080](http://localhost:8080).

## Structure du projet

```
src/main/java/com/centre/recherche/
+-- config/                     # Configuration Spring
|   +-- MvcConfig.java          # Configuration MVC (uploads)
|   +-- SecurityConfig.java     # Configuration Spring Security
+-- controllers/                # Controleurs Web et REST
|   +-- HomeController.java             # Pages publiques
|   +-- LoginController.java            # Authentification web
|   +-- RegistrationController.java     # Inscription
|   +-- ResearcherController.java       # Espace chercheur
|   +-- DocumentalisteController.java   # Espace documentaliste
|   +-- AdminController.java            # Espace admin
|   +-- StatisticsController.java       # Statistiques et exports
|   +-- AuthController.java             # API auth JWT
|   +-- ApiPublicationController.java   # API REST publications
|   +-- ApiCategoryController.java      # API REST categories
|   +-- ApiResearcherController.java    # API REST chercheurs
|   +-- ApiKeywordController.java       # API REST mots-cles
+-- models/                     # Entites JPA et enums
|   +-- Publication.java        # Publication (entite principale)
|   +-- Researcher.java         # Chercheur (profil etendu)
|   +-- User.java               # Utilisateur (authentification)
|   +-- Category.java           # Categorie disciplinaire
|   +-- Keyword.java            # Mot-cle
|   +-- PublicationType.java    # Enum : type de publication
|   +-- PublicationStatus.java  # Enum : statut du workflow
|   +-- Role.java               # Enum : role utilisateur
+-- repositories/               # Spring Data JPA
+-- services/                    # Logique metier
|   +-- PublicationService.java
|   +-- WorkflowService.java
|   +-- ResearcherService.java
|   +-- UserService.java
|   +-- CategoryService.java
|   +-- KeywordService.java
|   +-- EmailService.java
|   +-- StatisticsService.java
|   +-- search/
|   |   +-- TFIDFService.java           # Recherche TF-IDF
|   +-- export/
|       +-- PdfGeneratorService.java     # Export PDF (iText)
|       +-- ExcelGeneratorService.java   # Export Excel (Apache POI)
+-- security/                    # Securite JWT
    +-- JwtService.java
    +-- JwtAuthenticationFilter.java
    +-- CustomUserDetailsService.java
```

## Documentation API

L'interface Swagger UI est disponible apres demarrage :

- Swagger UI : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON : [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Roles et acces

| Role            | Acces                                                       |
|-----------------|-------------------------------------------------------------|
| Visiteur        | Catalogue public, recherche, profils chercheurs             |
| CHERCHEUR       | Espace chercheur : soumission, profil, exports, statistiques|
| DOCUMENTALISTE  | Espace documentaliste : validation des publications         |
| ADMIN           | Acces complet : gestion utilisateurs, categories, workflow |

## Auteur

Stage de fin d'etudes -- Centre de Recherche et d'Innovation

## Licence

Ce projet est realise dans le cadre d'un stage academique.