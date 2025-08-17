-- Étape 2 : colonnes premium
ALTER TABLE accounts ADD COLUMN is_premium INTEGER DEFAULT 0;
ALTER TABLE accounts ADD COLUMN uuid_online TEXT;
ALTER TABLE accounts ADD COLUMN premium_verified_at TIMESTAMP;
ALTER TABLE accounts ADD COLUMN premium_mode TEXT;

-- Index (idempotents)
CREATE INDEX IF NOT EXISTS idx_accounts_uuid_online ON accounts(uuid_online);
CREATE INDEX IF NOT EXISTS idx_accounts_is_premium ON accounts(is_premium);
