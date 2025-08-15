package com.faskin.auth.core;

import java.util.Optional;

public interface AccountRepository {
    boolean exists(String usernameLower);
    void create(String usernameLower, byte[] salt, byte[] hash);
    Optional<StoredAccount> find(String usernameLower);
    void updatePassword(String usernameLower, byte[] newSalt, byte[] newHash);

    final class StoredAccount {
        public final String usernameLower;
        public final byte[] salt;
        public final byte[] hash;
        public StoredAccount(String u, byte[] s, byte[] h) {
            this.usernameLower = u; this.salt = s; this.hash = h;
        }
    }
}
