# Validation Étape 2 — Auto-login premium

## 1) Pré-requis infra SAFE

* Proxy **Velocity** en **online-mode**.
* `velocity.toml` :

```toml
player-info-forwarding-mode = "modern"
forwarding-secret = "secret-unique"
```

* Backend Paper configuré pour accepter le forwarding (UUID, IP, textures).
* Le mode `modern` transmet les **UUID online** et les **properties** signées (textures) utilisées comme preuve premium.

## 2) Matrice de tests fonctionnels

| Cas | Contexte                                                | Attendu |
| --- | ------------------------------------------------------- | ------- |
| P1  | Compte **premium** via proxy OK (modern + secret)       | **Bypass `/login`** immédiat, logs `bypass ok`, `accounts.is_premium=1`, `uuid_online` & `premium_verified_at` remplis. |
| P2  | Premium mais **forwarding désactivé / secret invalide** | **Pas de bypass**, raison `forwarding-missing`, log `WARN`. |
| P3  | Premium, forwarding OK mais **textures absentes**       | **Pas de bypass**, raison `no-textures`. |
| C1  | Joueur **crack**                                        | Flux Étape 1 : `/register` puis `/login` obligatoire, garde-fous pré-auth actifs. |
| A1  | `premium.mode = SPIGOT_FALLBACK`                        | **Bypass désactivé**, raison `fallback-mode`, Étape 1 intacte. |
| U1  | `/premium unlink` (self)                                | Compte redevient **non premium** ; prochain join ⇒ requiert `/login`. |
| U2  | `/premium unlink <player>` (admin)                      | Idem offline/online ; purge immédiate de l’état premium si joueur en ligne. |
| R1  | Double connexion / reconnexion rapide                   | Une seule session active, pas de fuite d’état. |
| S1  | Plugins tiers modifiant `PlayerProfile.setTextures(...)`| **Refuser** `PROXY_SAFE` (profil invalide). |

## 3) Checks sécurité

* Forwarding **obligatoire** (modern ou legacy) pour activer le bypass premium.
* Aucune requête Mojang côté backend : seules les données signées du proxy sont utilisées.
* Étape 1 reste la **source de vérité** pour les comptes crack (verrous, sessions, `/register` & `/login`).

## 4) Perf & robustesse

* Objectif : **P95 PRE_AUTH < 100 ms**, aucune chute de TPS.
* Vérifier qu’aucune écriture DB n’est faite en **main-thread** (toutes en async).
* Métriques à relever (`console` ou `/faskin stats`) :
  * `bypass_ok_total`
  * `bypass_refused_total` + raisons
  * `preauth_ms_avg`
  * `preauth_ms_p95`

## 5) Logs attendus (exemples)

* `INFO [Faskin/Premium] bypass ok name=<n> uuidOnline=<XXXXXX> mode=PROXY_SAFE`
* `WARN [Faskin/Premium] bypass refused reason=forwarding-missing name=<n>`
* `WARN [Faskin/Premium] bypass refused reason=no-textures name=<n>`

## 6) Validation (à cocher)

- [ ] Cas P1–S1 conformes
- [ ] Aucune régression Étape 1 : cracks bloqués sans `/login`
- [ ] Forwarding mal configuré ⇒ refus explicite
- [ ] Perf : PRE_AUTH P95 < 100 ms, TPS stable
- [ ] CI verte, artefact `Faskin-0.1.0-T2.6-SNAPSHOT.jar`
- [ ] README/ROADMAP/CHANGELOG/Gradle à jour

