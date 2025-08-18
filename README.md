# Faskin
### Une solution d'authentification tout-en-un pour les serveurs Spigot (Auth, Premium Auto-Login & Skins).

## Description
Faskin est un plugin Spigot conçu pour résoudre un dilemme fondamental pour les administrateurs de serveurs Minecraft : comment autoriser les joueurs utilisant des versions non officielles (cracks) pour maximiser l'accessibilité, tout en garantissant la sécurité du serveur et en offrant une expérience utilisateur fluide et agréable pour les joueurs possédant un compte premium. Faskin apporte une solution intégrée qui sécurise votre serveur via un système d'authentification robuste pour les joueurs non-premium, tout en identifiant automatiquement les joueurs premium pour leur permettre une connexion instantanée et restaurer leur skin, créant ainsi un environnement transparent et sécurisé pour tous.

## Fonctionnalités Clés
- **Système d'Authentification Sécurisé :** Les joueurs sans compte premium doivent s'enregistrer avec une commande `/register` et se connecter avec `/login`. Les mots de passe sont hachés en utilisant l'algorithme bcrypt, la norme de l'industrie, pour garantir la sécurité des comptes.
- **Connexion Automatique pour les Comptes Premium :** Faskin vérifie de manière asynchrone auprès des serveurs de Mojang si un joueur possède un compte premium. Si c'est le cas, il est automatiquement connecté sans avoir besoin de taper de commande, offrant une expérience identique à celle d'un serveur en mode `online`.
- **Restauration des Skins pour les Comptes Premium :** Pour les serveurs en `online-mode=false`, les skins des joueurs premium sont souvent perdus. Faskin utilise ProtocolLib pour intercepter les paquets et réappliquer le skin correct à chaque joueur premium, restaurant ainsi l'identité visuelle de chacun.

## Installation
1. Téléchargez la dernière version de `Faskin.jar` depuis la page de releases.
2. Placez le fichier `Faskin.jar` dans le dossier `/plugins` de votre serveur Spigot 1.21+.
3. **Important :** Assurez-vous d'avoir déjà installé la dernière version de [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/), qui est une dépendance obligatoire.
4. Redémarrez votre serveur. Le dossier de configuration de Faskin sera généré.

## Configuration
Après le premier démarrage, un dossier `Faskin` contenant un fichier `config.yml` sera créé. L'étape de configuration la plus importante est de renseigner les informations d'accès à votre base de données. Faskin supporte MySQL (recommandé) et SQLite (pour les configurations simples). Assurez-vous de configurer cette section avant que les joueurs ne rejoignent le serveur.

## Commandes et Permissions

| Commande | Description | Permission |
| --- | --- | --- |
| `/register <mdp> <mdp>` | Crée un compte sur le serveur. | `faskin.command.register` |
| `/login <mdp>` | Se connecte à votre compte. | `faskin.command.login` |
| `/changepassword <ancien> <nouveau>` | Change le mot de passe de votre compte. | `faskin.command.changepassword` |
| `/skin update` | Force la mise à jour de votre skin. | `faskin.command.skin.update` |

## Dépendances
- **ProtocolLib :** Faskin requiert ProtocolLib pour fonctionner, notamment pour la restauration des skins. Sans cette dépendance, le plugin ne s'activera pas.
