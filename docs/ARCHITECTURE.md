# ARCHITECTURE — Faskin

Couches:
- core (cycle de vie, config, i18n)
- auth (Étape 1)
- premium-link (Étape 2)
- skin-bridge (Étape 3)

Événements cibles (Étape 1) : AsyncPlayerPreLoginEvent, PlayerLoginEvent, PlayerJoinEvent, PlayerMoveEvent, PlayerInteractEvent, AsyncPlayerChatEvent, EntityDamageEvent, PlayerCommandPreprocessEvent.

Stockage: SQLite par défaut (`plugins/Faskin/faskin.db`).

## Services (Étape 1)
- `AuthServiceRegistry` centralise `AccountRepository` et la table d’états en mémoire.
- `AccountRepository` (interface) → impl. temp `InMemoryAccountRepository`. La version SQLite remplace l’impl sans casser l’API.
