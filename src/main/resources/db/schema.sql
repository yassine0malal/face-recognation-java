-- ============================================
-- Schéma de base de données SQLite
-- Facial Access System
-- ============================================

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS USERS (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    role TEXT DEFAULT 'user',
    email TEXT,
    face_vector BLOB,
    qr_code_data TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1
);

-- Table des logs d'accès
CREATE TABLE IF NOT EXISTS ACCESS_LOGS (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    status TEXT NOT NULL CHECK(status IN ('GRANTED', 'DENIED')),
    confidence_score REAL,
    identification_method TEXT CHECK(identification_method IN ('FACE', 'QR')),
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USERS(id)
);

-- Table des administrateurs
CREATE TABLE IF NOT EXISTS ADMIN (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    locked_until DATETIME
);

-- Insertion du compte admin par défaut
-- Mot de passe: admin123 (hashé en SHA-256)
INSERT OR IGNORE INTO ADMIN (id, username, password_hash, failed_attempts, locked_until)
VALUES (
    1,
    'admin',
    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
    0,
    NULL
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_users_email ON USERS(email);
CREATE INDEX IF NOT EXISTS idx_access_logs_user_id ON ACCESS_LOGS(user_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_accessed_at ON ACCESS_LOGS(accessed_at);
CREATE INDEX IF NOT EXISTS idx_admin_username ON ADMIN(username);
