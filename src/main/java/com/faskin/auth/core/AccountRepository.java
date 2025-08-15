package com.faskin.auth.core;

import java.util.Optional;

public interface AccountRepository {
    boolean exists(String usernameLower);
    void create(String usernameLower, byte[] salt, byte[] hash);
    Optional<StoredAccount> find(String usernameLower);
    void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash);

    // Sessions
    void updateLastLoginAndIp(String usernameLower, String ip, long epochSeconds);
    Optional<SessionMeta> getSessionMeta(String usernameLower);

    // Anti-bruteforce
    boolean isLocked(String usernameLower);
    void registerFailedAttempt(String usernameLower, int max, long lockSeconds);
    void resetFailures(String usernameLower);
    long lockRemainingSeconds(String usernameLower); // 0 si pas lock

    // Admin & stats
    int countAccounts();
    int countLockedActive(long nowEpochSeconds);
    Optional<AdminInfo> adminInfo(String usernameLower);

    final class StoredAccount {
        public final String usernameLower; public final byte[] salt; public final byte[] hash;
        public StoredAccount(String u, byte[] s, byte[] h) { this.usernameLower = u; this.salt = s; this.hash = h; }
    }
    final class SessionMeta {
        public final String lastIp; public final long lastLoginEpoch;
        public SessionMeta(String ip, long ts) { this.lastIp = ip; this.lastLoginEpoch = ts; }
    }
    final class AdminInfo {
        public final boolean exists;
        public final String lastIp;
        public final long lastLoginEpoch;
        public final int failedCount;
        public final long lockedUntilEpoch;
        public AdminInfo(boolean ex, String ip, long last, int fails, long lock) {
            this.exists = ex; this.lastIp = ip; this.lastLoginEpoch = last; this.failedCount = fails; this.lockedUntilEpoch = lock;
        }
    }
}
