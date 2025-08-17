# Roadmap Faskin

## Étape 1 — Auth offline (DONE ✅)
- [x] `/register`, `/login`, stockage sécurisé (hash + salt), timeout, blocages pré-auth
- [x] Anti-bruteforce (cooldown, locks), rappels ActionBar/chat
- [x] Commandes admin (`/faskin status|unlock|stats`)
- [x] CI build (main) + **CI release taggée** (ce ticket) ✅

## Étape 2 — Auto-login Premium (DONE ✅ — version 0.1.0)

### Objectif
Permettre aux comptes **premium vérifiés** d’entrer **sans mot de passe**, tout en préservant l’authentification `/register` + `/login` pour les joueurs **crack** (Étape 1 intacte).

### Prérequis & sécurité
- **Mode recommandé**: réseau proxy (Velocity/Waterfall) en **online-mode** avec **player information forwarding** activé pour transmettre IP, **UUID online** et **properties** (textures). Sans cela, Faskin **n’active pas** le bypass premium.  
  _Raison_ : la preuve premium robuste est faite côté Mojang/proxy ; côté backend on se fie aux données **signées** transmises (évite spoof/mitm). [Ref. Velocity forwarding], [Ref. FastLogin IP verify].  
- **Spigot seul**: pas de bypass (trop risqué). On peut **auto-register** optionnellement, mais **/login** reste obligatoire.

### Modèle de données (migration `002_step2_premium.sql`)
- `accounts`
  - `is_premium` (bool/int) — défaut 0
  - `uuid_online` (text/varchar) — nullable
  - `premium_verified_at` (timestamp) — nullable
  - `premium_mode` (enum texte: `PROXY_SAFE` | `SPIGOT_FALLBACK`)
  - Index sur (`uuid_online`), (`is_premium`)
- `sessions`: inchangé (Étape 1)

### Flux & états
1) `JOINING → PRE_AUTH` (verrous Étape 1 actifs)  
2) **Détection premium**:
   - Si **PROXY_SAFE** + `uuid_online` présent + properties `textures` signées → **BYPASS**  
   - Sinon → `LOGIN_REQUIRED` (flux Étape 1)
3) `AUTHENTICATED` (création/renouvellement session IP si activée)

Flux simplifié : `JOINING → PRE_AUTH → (PREMIUM_SAFE ? BYPASS : LOGIN_REQUIRED) → AUTHENTICATED`.

### Détection premium (règles)
- Source: données de forwarding du proxy (UUID online + properties).
- Inspection de `PlayerProfile#getTextures()` au `PlayerJoinEvent` : absence de skin ou profil non signé ⇒ pas de preuve premium.
- Échec bypass si:
  - pas d’IP-forwarding,
  - pas d’UUID online,
  - pas de properties signées (textures),
  - mode `SPIGOT_FALLBACK`.
- À chaque succès: maj `accounts.is_premium=1`, `uuid_online`, `premium_verified_at`.

### Configuration (ajouts)
```yaml
premium:
  enabled: true
  mode: PROXY_SAFE            # PROXY_SAFE | SPIGOT_FALLBACK
  skip_password: true         # bypass /login si premium SAFE
  auto_register: true         # créer le compte si absent lorsque premium SAFE
  require_ip_forwarding: true # interdit bypass si forwarding manquant
session:
  ttl_minutes: 60
  allow_ip_session: true
```

### Commandes & permissions

* `/premium status` — affiche état premium/bypass, mode, dernière vérif (perm: `faskin.premium.status`)
* `/premium unlink [player]` — force retour au mot de passe (self/admin)
  * `faskin.premium.unlink.self`, `faskin.premium.unlink.other`

### Logs & observabilité

* `INFO` — bypass premium OK: `<name> <uuid_online[..8]> (mode=PROXY_SAFE)`
* `WARN` — bypass refusé: raison (`no-forwarding`, `no-textures`, `fallback-mode`, …)
* `METRICS` — `bypass_ok_total`, `bypass_refused_total`, `preauth_ms_avg`

### Compatibilité & futur (Étape 3 Skins)

* 1.21: utiliser l’API `PlayerProfile`/`PlayerTextures` pour manipuler/inspecter les textures. Attention: setter des textures **invalide** les attributs signés du profil officiel (comportement API).
* Cible Étape 3: intégration d’un service de skins offline compatible 1.21 (type SkinsRestorer) + cache local, avec options de restauration et de skin custom. (Étape 2 n’applique pas encore de skins.)

### Tâches (checklist)

* [x] Migration DB `002_step2_premium.sql` (champs + index)
* [x] Service `PremiumDetector` (évalue `PREMIUM_SAFE | NOT_PREMIUM`)
* [x] `AuthBypassService` (marque AUTHENTICATED, crée session si besoin)
* [x] Listeners `AsyncPlayerPreLoginEvent`/`PlayerLoginEvent` (intégration)
* [x] Config & messages (nouveaux blocs YAML + i18n)
* [x] Commandes `/premium status|unlink` (+ tab-complete, perms)
* [x] Logs & métriques
* [x] Doc README: guide proxy (online-mode, forwarding) + exemples
* [x] Tests manuels (voir Validation)

### Critères d’acceptation (AC)

* [x] Premium via proxy SAFE → entrée directe **sans `/login`** ; `accounts.is_premium=1`, `uuid_online` enregistré
* [x] Joueur crack → ne peut **jamais** bypass `/login`
* [x] Proxy mal configuré (pas de forwarding/properties) → **pas de bypass** et message explicite
* [x] Aucun blocage du **tick** (I/O async), P95 PRE_AUTH < 100 ms
* [x] Étape 1 inchangée fonctionnellement (aucune régression)

### Validation (tests manuels)

* Proxy OK (online-mode + forwarding) → premium bypass
* Proxy KO (forwarding off) → bypass refusé + log WARN
* `unlink` → premium redevient mot de passe
* Déconnexions en PRE_AUTH → cleanup sessions
* Double connexion concurrente → une seule session active (Étape 1)

_Notes sources_ :
- **Forwarding/UUID/skins via proxy** (Velocity → backend) : Paper/Velocity docs.  
- **Vérif IP & sécurité FastLogin** (risque MITM si pas d’IP-forwarding) : discussions/README du projet.  
- **Auth offline (sessions/2FA) — référence de bonnes pratiques** : AuthMe Reloaded.  
- **API 1.21 profils/textures** : `PlayerProfile.update()` et `PlayerTextures`.  
- **Skins offline & compat 1.21** : SkinsRestorer (site & release récente).

### T2.4 — Finitions Étape 2

* [x] Messages i18n premium complets (checking/ok/refus + raisons).
* [x] Garde-fous forwarding/textures/fallback câblés.
* [x] Métriques & logs structurés (+ /faskin stats étendu).
* [x] README (section proxy Velocity + exemples).
* [x] CHANGELOG renseigné.
* [x] CI (artefact versionné).
* [x] Aucune régression Étape 1.

## Étape 3 — Skins premium en offline (Backlog)
- [ ] Récup textures signées + application (Paper API / ProtocolLib en option)
- [ ] Opt-in joueur + préférences
- [ ] Compat packs de ressources, schemas de reset arène (si multi-jeux)

> Rappel : on **ajoute** seulement des items; on ne supprime rien.

## Prochaines étapes
- Créer **tickets d’implémentation Étape 2** (T2.1 → T2.6) alignés sur la checklist ci-dessus (sans toucher à l’Étape 1).
- Mettre à jour `README.md` avec un encart **“Mode PROXY_SAFE recommandé”** + extrait de config Velocity (`player-info-forwarding`) pour éviter les faux positifs et sécuriser le bypass.
- [x] T2.1 — Base auto-login premium
- [x] T2.2 — Détection premium via forwarding (UUID + textures)
- [x] T2.3 — Intégration bypass `/login`
- [x] T2.4 — Finitions Étape 2 (UX, métriques, garde-fous)
 - [x] T2.5 — Commandes premium (status/unlink, perms)
 - [x] T2.6 — Validation finale & sécurité
