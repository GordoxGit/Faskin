# ROADMAP — Faskin
> Règle : **ajouter uniquement**, ne rien supprimer une fois publié.

## Étape 1 — Auth offline
- [x] TICKET-101: Core + bootstrap plugin (services, commandes, logs)
- [x] TICKET-102: DAO + schéma SQLite + PBKDF2
- [x] TICKET-102-HOTFIX: Build Gradle 9 (Shadow 9) + CI clean
- [x] TICKET-103: State machine & sessions IP
- [x] TICKET-104: Restrictions pré-auth (blocages + whitelist)
- [x] TICKET-105: UX commandes + messages + i18n (prefix/couleurs, actionbar, rate-limit)
- [x] TICKET-106: Timeout & anti-bruteforce (lock, cooldown)
- [ ] TICKET-107: Admin (/faskin reload|status)
- [ ] TICKET-108: CI release

## Étape 2 — Auto-login premium
- [ ] Détection premium + bypass auth (toggle)

## Étape 3 — Skins premium en offline
- [ ] Récupération/apply skins (toggle)
