-- ============================================
-- Schéma de base de données SQLite
-- Facial Access System
-- Architecture avec héritage : PERSONNE -> ADMIN / UTILISATEUR
-- ============================================

-- Table mère PERSONNE (attributs communs)
CREATE TABLE IF NOT EXISTS PERSONNE (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1,
    type TEXT NOT NULL CHECK(type IN ('ADMIN', 'UTILISATEUR'))
);

-- Table fille ADMIN (attributs spécifiques aux administrateurs)
CREATE TABLE IF NOT EXISTS ADMIN (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    locked_until DATETIME,
    FOREIGN KEY (id) REFERENCES PERSONNE(id) ON DELETE CASCADE
);

-- Table fille UTILISATEUR (attributs spécifiques aux utilisateurs)
CREATE TABLE IF NOT EXISTS UTILISATEUR (
    id INTEGER PRIMARY KEY,
    role TEXT DEFAULT 'user',
    face_vector BLOB,
    qr_code_data TEXT,
    FOREIGN KEY (id) REFERENCES PERSONNE(id) ON DELETE CASCADE
);

-- Table des logs d'accès (inchangée)
CREATE TABLE IF NOT EXISTS ACCESS_LOGS (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    status TEXT NOT NULL CHECK(status IN ('GRANTED', 'DENIED')),
    confidence_score REAL,
    identification_method TEXT CHECK(identification_method IN ('FACE', 'QR')),
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES UTILISATEUR(id)
);

-- Insertion du compte admin par défaut
-- 1. Insérer dans PERSONNE
INSERT OR IGNORE INTO PERSONNE (id, full_name, email, type, is_active)
VALUES (1, 'Administrateur', 'admin@facialaccess.com', 'ADMIN', 1);

-- 2. Insérer dans ADMIN
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
CREATE INDEX IF NOT EXISTS idx_personne_email ON PERSONNE(email);
CREATE INDEX IF NOT EXISTS idx_personne_type ON PERSONNE(type);
CREATE INDEX IF NOT EXISTS idx_admin_username ON ADMIN(username);
CREATE INDEX IF NOT EXISTS idx_utilisateur_id ON UTILISATEUR(id);
CREATE INDEX IF NOT EXISTS idx_access_logs_user_id ON ACCESS_LOGS(user_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_accessed_at ON ACCESS_LOGS(accessed_at);
