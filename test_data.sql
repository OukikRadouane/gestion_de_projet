-- SQL Script to populate the database with test data for RBAC testing
-- This script cleans existing data and inserts new test data

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE task;
TRUNCATE TABLE sprint;
TRUNCATE TABLE project;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Create Users
-- Password is 'password123' (or whatever you entered) hashed with SHA-256 as produced by your app: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
INSERT INTO users (id, username, password_hash, email, role, created_at) VALUES 
(1, 'admin', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'admin@example.com', 'ADMIN', NOW()),
(2, 'owner', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'owner@example.com', 'PRODUCT_OWNER', NOW()),
(3, 'scrum', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'scrum@example.com', 'SCRUM_MASTER', NOW()),
(4, 'dev1', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'dev1@example.com', 'USER', NOW()),
(5, 'dev2', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'dev2@example.com', 'USER', NOW());

-- 2. Create Projects
INSERT INTO project (id, name, description, creator_id, created_at, start_date, end_date) VALUES 
(1, 'Application Mobile E-Commerce', 'Développement d\'une application mobile de vente en ligne sous Android/iOS.', 2, NOW(), '2025-01-01', '2025-06-30'),
(2, 'Refonte Site Web Intranet', 'Modernisation du portail interne pour améliorer l\'expérience collaborateur.', 1, NOW(), '2025-02-15', '2025-12-31');

-- 3. Create Sprints for Project 1
INSERT INTO sprint (id, name, project_id, goal, status, start_date, end_date) VALUES 
(1, 'Sprint 1 - Authentification', 1, 'Mise en place de la connexion et de l\'inscription.', 'PLANNED', '2025-01-01', '2025-01-14'),
(2, 'Sprint 2 - Catalogue Produit', 1, 'Affichage des articles et recherche.', 'PLANNED', '2025-01-15', '2025-01-28');

-- 4. Create Tasks for Project 1 / Sprint 1
INSERT INTO task (title, description, status, priority, deadline, assignee_id, sprint_id, project_id) VALUES 
('Conception Base de Données', 'Créer le schéma SQL pour les utilisateurs et sessions.', 'DOING', 'HIGH', '2025-01-05', 4, 1, 1),
('Intégration Login FXML', 'UI pour l\'écran de connexion.', 'TO_DO', 'MEDIUM', '2025-01-07', 5, 1, 1),
('API d\'Authentification', 'Service backend pour valider les credentials.', 'TO_DO', 'HIGH', '2025-01-10', 4, 1, 1);

-- 5. Create Tasks in Backlog (no sprint)
INSERT INTO task (title, description, status, priority, deadline, assignee_id, sprint_id, project_id) VALUES 
('Configuration CI/CD', 'Mettre en place Jenkins pour le projet 2.', 'BACKLOG', 'MEDIUM', '2025-03-01', NULL, NULL, 2),
('Rédaction spécifications techniques', 'Détails de l\'architecture.', 'BACKLOG', 'LOW', '2025-04-01', NULL, NULL, 2);
