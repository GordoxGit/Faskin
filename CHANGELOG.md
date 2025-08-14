## 0.0.1
- Init: skeleton HeneriaCore (main class, plugin.yml, config.yml, CI).

## 0.0.2
- Add SQLite database layer and AuthManager with sessions.
- Implement PBKDF2 password hashing and auth commands.
- Fix CI to use gradle/gradle-build-action@v3.

## 0.0.3
- Add PremiumDetector (sessionserver probe) with rate limiting and backoff.
- Integrate auto-login via PremiumAuthService and PremiumLoginEvent.

## 0.0.4
- Add SkinService with ProtocolLib and Paper reflection fallback.
- Cache signed textures and apply them on AuthPostLoginEvent.

## 0.0.5
- Add HttpClientWrapper with rate limiter, backoff strategy, circuit breaker and metrics.
- Expose metrics through DebugInfoProvider and extend configuration.

## 0.0.6
- Introduce claim flow with tokenized skin verification.
- Add ClaimManager, commands and configuration.

## 0.0.7
- Add per-player opt-in/out preferences stored in SQLite.
- Implement `/heneria optin`, `/heneria optout`, `/heneria prefs` and admin `/heneria debug`.
- Skip auto skin apply, auto-login and claim flows when players opt out.
