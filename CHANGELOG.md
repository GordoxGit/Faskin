## 0.0.1
- Init: skeleton HeneriaCore (main class, plugin.yml, config.yml, CI).

## 0.0.2
- Add SQLite database layer and AuthManager with sessions.
- Implement PBKDF2 password hashing and auth commands.
- Fix CI to use gradle/gradle-build-action@v3.

## 0.0.3
- Add PremiumDetector (sessionserver probe) with rate limiting and backoff.
- Integrate auto-login via PremiumAuthService and PremiumLoginEvent.
