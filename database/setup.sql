-- Run locally as a MySQL administrator. Use a unique password.
CREATE DATABASE payease CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'payease_user'@'%' IDENTIFIED BY 'replace_with_a_long_database_password';
GRANT ALL PRIVILEGES ON payease.* TO 'payease_user'@'%';
FLUSH PRIVILEGES;
