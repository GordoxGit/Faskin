CREATE TABLE IF NOT EXISTS player_flags (
  uuid TEXT PRIMARY KEY,
  opted_out INTEGER DEFAULT 0,
  auto_apply_skin INTEGER DEFAULT 1,
  updated_at INTEGER
);
