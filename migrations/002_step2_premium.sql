-- 002_step2_premium.sql
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS is_premium INTEGER DEFAULT 0;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS uuid_online TEXT;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS premium_verified_at TIMESTAMP;
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS premium_mode TEXT;
CREATE INDEX IF NOT EXISTS idx_accounts_uuid_online ON accounts(uuid_online);
CREATE INDEX IF NOT EXISTS idx_accounts_is_premium ON accounts(is_premium);
