-- Cria o banco e o usuário do SGHA em um MySQL 8 já instalado.
-- Uso: mysql -u root -p < db/criar-banco.sql
-- As tabelas são criadas automaticamente pelo Hibernate na primeira execução.

CREATE DATABASE IF NOT EXISTS sgha
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'sgha'@'localhost' IDENTIFIED BY 'sgha123';
GRANT ALL PRIVILEGES ON sgha.* TO 'sgha'@'localhost';