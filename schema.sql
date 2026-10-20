-- CareerVerse AI database schema
-- You normally do NOT need to run this file: the app creates these tables
-- automatically when it starts (see config/SchemaInitializer.java).
-- Run it by hand only if you want to create the tables yourself.
--
-- Local MySQL: uncomment the next two lines.
-- CREATE DATABASE IF NOT EXISTS careerverse_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE careerverse_ai;
-- Aiven: run it inside the default database (defaultdb).

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  email_verified BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS profiles (
  user_id INT PRIMARY KEY,
  branch VARCHAR(160) DEFAULT '',
  current_year INT DEFAULT 1,
  interests TEXT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS academic_records (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  academic_year INT NOT NULL,
  cgpa DECIMAL(4,2) NOT NULL,
  UNIQUE KEY uq_user_year (user_id, academic_year),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS profile_skills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  skill_name VARCHAR(100) NOT NULL,
  skill_level INT NOT NULL DEFAULT 1,
  UNIQUE KEY uq_user_skill (user_id, skill_name),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assessments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  logical_score INT DEFAULT 0,
  programming_score INT DEFAULT 0,
  communication_score INT DEFAULT 0,
  mathematics_score INT DEFAULT 0,
  creativity_score INT DEFAULT 0,
  analytical_score INT DEFAULT 0,
  completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  token_hash VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS career_progress (
  user_id INT PRIMARY KEY,
  career_id VARCHAR(80),
  roadmap_progress INT DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS leetcode_profiles (
  user_id INT PRIMARY KEY,
  username VARCHAR(60) NOT NULL,
  linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  base_easy INT DEFAULT 0,
  base_medium INT DEFAULT 0,
  base_hard INT DEFAULT 0,
  base_sql INT DEFAULT 0,
  baseline_set BOOLEAN DEFAULT FALSE,
  last_synced TIMESTAMP NULL,
  stats_json MEDIUMTEXT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS leetcode_daily (
  user_id INT NOT NULL,
  day DATE NOT NULL,
  submissions INT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id, day),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
