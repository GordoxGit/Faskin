CREATE TABLE IF NOT EXISTS claim_tokens (
  id TEXT PRIMARY KEY,
  token TEXT NOT NULL,
  requester_uuid TEXT NOT NULL,
  target_name TEXT NOT NULL,
  created_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  state TEXT NOT NULL,
  attempts INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_claim_requester ON claim_tokens(requester_uuid);
