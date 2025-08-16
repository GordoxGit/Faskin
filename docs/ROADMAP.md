# Roadmap Faskin

## Étape 1 — Auth offline (DONE ✅)
- [x] `/register`, `/login`, stockage sécurisé (hash + salt), timeout, blocages pré-auth
- [x] Anti-bruteforce (cooldown, locks), rappels ActionBar/chat
- [x] Commandes admin (`/faskin status|unlock|stats`)
- [x] CI build (main) + **CI release taggée** (ce ticket) ✅

## Étape 2 — Auto-login premium (Backlog)
- [ ] Détection premium asynchrone (profil Mojang) + cache
- [ ] Bypass login si premium vérifié, sinon flux Étape 1
- [ ] Paramètres de sûreté (timeouts, retries, circuit breaker HTTP)
- [ ] Hooks d’événements (join/quit) idempotents et tick-safe
- [ ] Journalisation & métriques (req/s, taux premium, erreurs API)

## Étape 3 — Skins premium en offline (Backlog)
- [ ] Récup textures signées + application (Paper API / ProtocolLib en option)
- [ ] Opt-in joueur + préférences
- [ ] Compat packs de ressources, schemas de reset arène (si multi-jeux)

> Rappel : on **ajoute** seulement des items; on ne supprime rien.
