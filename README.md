# Scientific Publication Manager

[![Coverage](https://img.shields.io/badge/JaCoCo-87.4%25-brightgreen)](https://github.com/mansoursaadou84/scientific-publication-manager)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Academic-blue)](LICENSE)

Application web de gestion des ressources documentaires et des publications
scientifiques pour un centre de recherche.

## Aperçu de l'interface

| Portail public | Tableau de bord Administrateur |
|:---:|:---:|
| ![Portail public](docs/screenshots/home.png) | ![Dashboard Admin](docs/screenshots/dashboard.png) |

| Espace Chercheur | Moteur de recherche TF-IDF |
|:---:|:---:|
| ![Espace Chercheur](docs/screenshots/researcher.png) | ![Recherche TF-IDF](docs/screenshots/search.png) |

## Fonctionnalités

- **Catalogue public** avec recherche avancée (TF-IDF) et filtres (type, catégorie, année)
- **Espace chercheur** : soumission, modification, suivi des publications, profil, export PDF/Excel
- **Espace documentaliste** : validation et gestion du workflow éditorial
- **Espace administrateur** : gestion des utilisateurs, catégories, mots-clés, workflow
- **Workflow de validation** : Brouillon → Soumise → En validation → Approuvée → Publiée
- **Export PDF et Excel** des rapports et statistiques
- **Notifications par email** lors des changements de statut
- **API REST sécurisée** par JWT avec documentation Swagger/OpenAPI

## Technologies

| Composant | Technologie |
|---|---|
| Backend | Spring Boot 3.2.5, Java 17 |
| Sécurité | Spring Security, JWT (jjwt 0.11.5) |
| Base de données | MySQL 8, Spring Data JPA |
| Moteur de templates | Thymeleaf + Layout Dialect |
| Recherche | TF-IDF (implémentation native Java) |
| Export PDF | iText 7 |
| Export Excel | Apache POI 5.2.5 |
| Documentation API | SpringDoc OpenAPI 2.3.0 |
| Email | Spring Mail (SMTP) |
| Conteneurisation | Docker, Docker Compose |

## Démarrage rapide

### Option 1 : Docker (recommandé)

```bash
git clone https://github.com/mansoursaadou84/scientific-publication-manager.git
cd scientific-publication-manager
cp .env.example .env
# Éditer .env avec vos identifiants (voir Variables d'environnement ci-dessous)
docker compose up --build -d
```

L'application est accessible sur [http://localhost:8080](http://localhost:8080).

### Option 2 : Développement local

```bash
git clone https://github.com/mansoursaadou84/scientific-publication-manager.git
cd scientific-publication-manager
```

**Prérequis** : Java 17+, Maven 3.9+, MySQL 8+

**Variables d'environnement requises** (à définir dans `application.properties`) :

| Variable | Description | Exemple |
|---|---|---|
| `spring.datasource.password` | Mot de passe MySQL | `your_mysql_password` |
| `app.jwt.secret` | Clé secrète JWT (min. 256 bits) | `your-256-bit-secret-key-here` |
| `spring.mail.username` | Identifiant SMTP Gmail | `your.email@gmail.com` |
| `spring.mail.password` | Mot de passe d'application Gmail | `your-app-password` |

```bash
# Configurer l'application
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Éditer application.properties avec vos valeurs

# Créer la base de données
mysql -u root -p -e "CREATE DATABASE scientific_publication_db;"

# Lancer les tests avec couverture JaCoCo
mvn test jacoco:report

# Lancer l'application
mvn spring-boot:run
```

## Structure du projet

```
src/main/java/com/centre/recherche/
├── config/                     # Configuration Spring
│   ├── MvcConfig.java          # Configuration MVC (uploads)
│   └── SecurityConfig.java     # Configuration Spring Security
├── controllers/                # Contrôleurs Web et REST
│   ├── HomeController.java             # Pages publiques
│   ├── LoginController.java            # Authentification web
│   ├── RegistrationController.java     # Inscription
│   ├── ResearcherController.java       # Espace chercheur
│   ├── DocumentalisteController.java   # Espace documentaliste
│   ├── AdminController.java            # Espace admin
│   ├── StatisticsController.java       # Statistiques et exports
│   ├── AuthController.java             # API auth JWT
│   ├── ApiPublicationController.java   # API REST publications
│   ├── ApiCategoryController.java      # API REST catégories
│   ├── ApiResearcherController.java    # API REST chercheurs
│   └── ApiKeywordController.java       # API REST mots-clés
├── models/                     # Entités JPA et enums
│   ├── Publication.java        # Publication (entité principale)
│   ├── Researcher.java         # Chercheur (profil étendu)
│   ├── User.java               # Utilisateur (authentification)
│   ├── Category.java           # Catégorie disciplinaire
│   ├── Keyword.java            # Mot-clé
│   ├── PublicationType.java    # Enum : type de publication
│   ├── PublicationStatus.java  # Enum : statut du workflow
│   └── Role.java               # Enum : rôle utilisateur
├── repositories/               # Spring Data JPA
├── services/                    # Logique métier
│   ├── PublicationService.java
│   ├── WorkflowService.java
│   ├── ResearcherService.java
│   ├── UserService.java
│   ├── CategoryService.java
│   ├── KeywordService.java
│   ├── EmailService.java
│   ├── StatisticsService.java
│   └── search/
│       └── TFIDFService.java           # Recherche TF-IDF
│   └── export/
│       ├── PdfGeneratorService.java     # Export PDF (iText)
│       └── ExcelGeneratorService.java   # Export Excel (Apache POI)
└── security/                    # Sécurité JWT
    ├── JwtService.java
    ├── JwtAuthenticationFilter.java
    └── CustomUserDetailsService.java
```

## Tests et couverture

```bash
# Exécuter les tests unitaires
mvn test

# Générer le rapport de couverture JaCoCo
mvn test jacoco:report

# Consulter le rapport
open target/site/jacoco/index.html
```

**Résultats** : 72 tests JUnit 5 | Couverture globale : 87,4 %

## Documentation API

L'interface Swagger UI est disponible après démarrage :

- Swagger UI : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON : [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Rôles et accès

| Rôle | Accès |
|---|---|
| Visiteur | Catalogue public, recherche, profils chercheurs |
| CHERCHEUR | Espace chercheur : soumission, profil, exports, statistiques |
| DOCUMENTALISTE | Espace documentaliste : validation des publications |
| ADMIN | Accès complet : gestion utilisateurs, catégories, workflow |

## Auteur

**Saadou Issa Mamane Mansour** — Stage de fin d'études, Centre de Recherche et d'Innovation de Settat

Encadrant : Pr. Makroum El Mostafa

## Licence

Ce projet est réalisé dans le cadre d'un stage académique à la Faculté des Sciences et Techniques de Settat, Université Hassan 1er.