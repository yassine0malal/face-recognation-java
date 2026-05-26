-- 1. Activer impérativement les contraintes de clés étrangères
PRAGMA foreign_keys = ON;

-- 2. Création de la table Mère (Contient les données partagées)
CREATE TABLE IF NOT EXISTS PERSONNE (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active INTEGER DEFAULT 1,
    type TEXT NOT NULL CHECK(type IN ('ADMIN', 'UTILISATEUR'))
);

-- 3. Création de la table Fille UTILISATEUR (Hérite de PERSONNE via son ID)
CREATE TABLE IF NOT EXISTS UTILISATEUR (
    id INTEGER PRIMARY KEY,
    role TEXT DEFAULT 'user',
    face_image BLOB,
    face_vector BLOB,
    qr_code_data TEXT,
    FOREIGN KEY (id) REFERENCES PERSONNE(id) ON DELETE CASCADE
);

-- 4. Création de la table Fille ADMIN (Hérite de PERSONNE via son ID)
CREATE TABLE IF NOT EXISTS ADMIN (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    failed_attempts INTEGER DEFAULT 0,
    locked_until DATETIME,
    FOREIGN KEY (id) REFERENCES PERSONNE(id) ON DELETE CASCADE
);

-- 5. Création de la table dépendante des logs d'accès
CREATE TABLE IF NOT EXISTS ACCESS_LOGS (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    status TEXT NOT NULL CHECK(status IN ('GRANTED', 'DENIED')),
    confidence_score REAL,
    identification_method TEXT CHECK(identification_method IN ('FACE', 'QR')),
    accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES UTILISATEUR(id) ON DELETE CASCADE
);

-- 6. Insertion ordonnée des données d'initialisation
-- Étape A : On crée l'identité dans la table PERSONNE
INSERT OR IGNORE INTO PERSONNE (id, full_name, email, type, is_active)
VALUES (1, 'Administrateur', 'admin@facialaccess.com', 'ADMIN', 1);

-- Étape B : On lie le compte d'authentification dans la table ADMIN
INSERT OR IGNORE INTO ADMIN (id, username, password_hash, failed_attempts, locked_until)
VALUES (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 0, NULL);

-- 7. Création des Index pour optimiser les requêtes de recherche et de jointures
CREATE INDEX IF NOT EXISTS idx_personne_email ON PERSONNE(email);
CREATE INDEX IF NOT EXISTS idx_personne_type ON PERSONNE(type);
CREATE INDEX IF NOT EXISTS idx_admin_username ON ADMIN(username);
CREATE INDEX IF NOT EXISTS idx_utilisateur_id ON UTILISATEUR(id);
CREATE INDEX IF NOT EXISTS idx_access_logs_user_id ON ACCESS_LOGS(user_id);