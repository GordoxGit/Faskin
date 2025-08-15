-- Création de la table des comptes (référence runtime)
CREATE TABLE IF NOT EXISTS accounts(
  username_ci TEXT PRIMARY KEY,
  salt        BLOB NOT NULL,
  hash        BLOB NOT NULL,
  created_at  INTEGER NOT NULL,
  last_ip     TEXT,
  last_login  INTEGER,
  failed_count INTEGER DEFAULT 0,
  locked_until INTEGER
);
CREATE INDEX IF NOT EXISTS idx_accounts_locked ON accounts(locked_until);

