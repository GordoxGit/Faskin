# Feuille de Route du Projet Faskin

Ce document a pour but de suivre l'avancement du développement du plugin Faskin. Les fonctionnalités sont organisées par version et par phase, ce qui correspondra à la création des tickets de développement.

## Version 1.0 - Le Noyau Fonctionnel

Cette version initiale se concentre sur la mise en place des trois fonctionnalités fondamentales du plugin sur un serveur Spigot unique.

### Phase 1 : Mise en Place du Projet et de la Base de Données
- [ ] Création de la structure du projet Maven avec les dépendances requises.
- [ ] Implémentation d'un gestionnaire de base de données abstrait.
- [ ] Implémentation du support pour MySQL/MariaDB.
- [ ] Implémentation du support pour SQLite.
- [ ] Création du schéma de la table des joueurs et de la logique de requêtes (DAO).

### Phase 2 : Système d'Authentification de Base
- [ ] Implémentation de la commande `/register` avec hachage bcrypt.
- [ ] Implémentation de la commande `/login` avec comparaison de hash.
- [ ] Implémentation de la commande `/changepassword`.
- [ ] Mise en place de la logique de session pour les joueurs authentifiés.
- [ ] Implémentation des restrictions pour les joueurs non authentifiés (mouvement, chat, commandes, etc.).

### Phase 3 : Détection des Comptes Premium
- [ ] Création d'un service de communication asynchrone avec l'API de Mojang.
- [ ] Implémentation de la logique de vérification premium dans l'événement `AsyncPlayerPreLoginEvent`.
- [ ] Gestion du cache pour les réponses de l'API Mojang afin d'éviter le rate-limiting.
- [ ] Gestion de la transition automatique d'un compte enregistré comme "crack" vers le statut "premium".

### Phase 4 : Restauration des Skins
- [ ] Intégration de ProtocolLib et mise en place des listeners de paquets (`PLAYER_INFO`, `NAMED_ENTITY_SPAWN`).
- [ ] Création d'un service de récupération des données de skin via l'API de Mojang.
- [ ] Implémentation d'un système de cache (mémoire et disque) pour les données de skin.
- [ ] Logique d'application des skins sur les entités joueurs pour tous les clients.
- [ ] Implémentation de la commande `/skin update` pour rafraîchir le cache d'un joueur.

## Versions Futures (Idées et Améliorations)

- **Support BungeeCord/Velocity :** Créer un plugin compagnon pour le proxy afin de centraliser l'authentification et les sessions sur l'ensemble d'un réseau de serveurs.
- **Protection Anti-Bot :** Ajouter des mesures de protection lors de la connexion pour filtrer les vagues de bots (ex: captcha en jeu, vérification de vélocité).
- **Authentification à Deux Facteurs (2FA) :** Permettre aux joueurs de sécuriser davantage leur compte avec un code à usage unique (via des applications comme Google Authenticator).
- **Interface Graphique (GUI) :** Proposer des menus graphiques pour certaines actions.
- **Localisation (i18n) :** Permettre la traduction de tous les messages du plugin dans plusieurs langues.
