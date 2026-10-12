# GIK - Gestion des Tests de Flottabilité des Gilets

Une application web complète pour gérer le suivi des tests de flottabilité des gilets pour une école de voile.

## 🚀 Fonctionnalités

### Backend (Rust + Actix-web + SQLite)
- **Gestion des gilets** : CRUD complet (Créer, Lire, Mettre à jour, Supprimer)
- **Gestion des tests** : Enregistrement des tests de flottabilité avec résultat (OK/KO)
- **Historique** : Visualisation de tous les tests associés à un gilet
- **Recherche** : Recherche des gilets par numéro de série, modèle, taille ou état
- **Statistiques** : Tableau de bord avec indicateurs clés

### Frontend (HTML/CSS/JS)
- Interface moderne et réactive
- Navigation par onglets
- Formulaires de création/modification
- Notifications toast
- Design responsive (mobile-friendly)

## 📁 Structure du projet

```
gik_backend/
├── Cargo.toml                 # Dépendances Rust
├── Dockerfile                 # Configuration Docker
├── docker-compose.yml         # Orchestration Docker
├── .env                       # Variables d'environnement
├── .gitignore
├── README.md
├── src/
│   ├── main.rs                # Point d'entrée
│   ├── db.rs                  # Logique base de données (SQLite)
│   ├── models.rs              # Modèles de données
│   ├── routes/
│   │   ├── mod.rs             # Module des routes
│   │   ├── gilets.rs          # Routes pour les gilets
│   │   └── tests.rs           # Routes pour les tests
│   └── utils.rs               # Fonctions utilitaires
└── static/
    ├── index.html             # Page principale
    ├── css/
    │   └── style.css          # Styles CSS
    └── js/
        └── app.js              # Logique frontend
```

## 🛠️ Prérequis

- [Rust](https://www.rust-lang.org/tools/install) 1.70+
- [Cargo](https://doc.rust-lang.org/cargo/getting-started/installation.html)
- [Docker](https://docs.docker.com/get-docker/) (optionnel, pour le déploiement)

## 🏃‍♂️ Installation et exécution

### Méthode 1: Exécution locale (sans Docker)

```bash
# Cloner le dépôt
git clone https://github.com/PtiBzh/Gik.git
cd Gik

# Construire et exécuter
cargo run
```

L'application sera disponible à l'adresse : http://localhost:8080

### Méthode 2: Avec Docker

```bash
# Construire et démarrer le conteneur
docker-compose up -d --build

# Arrêter le conteneur
docker-compose down
```

L'application sera disponible à l'adresse : http://localhost:8080

## 📡 API REST

### Base URL: `/api`

### Gilets
- **GET** `/api/gilets` - Liste de tous les gilets
- **GET** `/api/gilets/{id}` - Détails d'un gilet
- **POST** `/api/gilets` - Créer un gilet
- **PUT** `/api/gilets/{id}` - Mettre à jour un gilet
- **DELETE** `/api/gilets/{id}` - Supprimer un gilet
- **GET** `/api/gilets/search?field={field}&value={value}` - Rechercher des gilets

### Tests
- **GET** `/api/tests` - Liste de tous les tests
- **GET** `/api/tests/{id}` - Détails d'un test
- **POST** `/api/tests` - Créer un test
- **PUT** `/api/tests/{id}` - Mettre à jour un test
- **DELETE** `/api/tests/{id}` - Supprimer un test
- **GET** `/api/tests/by-gilet/{gilet_id}` - Tests d'un gilet spécifique

## 📊 Modèles de données

### Gilet
```json
{
  "id": 1,
  "num_serie": "GIK-2024-001",
  "modele": "Mustang Survival",
  "taille": "M",
  "date_achat": "2024-01-15",
  "etat": "Bon",
  "dernier_test": "2024-06-01"
}
```

### Test de Flottabilité
```json
{
  "id": 1,
  "gilet_id": 1,
  "date_test": "2024-06-01",
  "resultat": "OK",
  "commentaire": "Test effectué dans de bonnes conditions",
  "responsable": "Jean Dupont"
}
```

## 🎨 Captures d'écran

L'application propose une interface moderne avec :
- Un tableau de bord avec onglets
- Des tableaux de données avec recherche
- Des formulaires modaux pour l'ajout/modification
- Un tableau de bord de statistiques

## 🔧 Configuration

### Variables d'environnement

Créer un fichier `.env` à la racine :

```env
DATABASE_URL=gik.db
RUST_LOG=actix_web=info
```

### Déploiement

Pour un déploiement en production :

1. **Construire l'image Docker** :
   ```bash
   docker build -t gik_backend .
   ```

2. **Exécuter le conteneur** :
   ```bash
   docker run -d -p 8080:8080 -v ./data:/usr/src/gik_backend/data gik_backend
   ```

3. **Avec Docker Compose** :
   ```bash
   docker-compose up -d
   ```

## 📝 Historique des versions

- **v0.1.0** : Version initiale avec gestion des gilets et tests

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
- Signaler des bugs
- Proposer des améliorations
- Envoyer des pull requests

## 📄 Licence

MIT License
