-- WeedX Database Schema
-- Version: 1.0
-- Description: Complete database structure for WeedX precision farming system

CREATE DATABASE IF NOT EXISTS weedx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weedx;

-- =====================================================
-- USERS & AUTHENTICATION
-- =====================================================

CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500) NULL,
    phone VARCHAR(20) NULL,
    last_login DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- FARM INFORMATION
-- =====================================================

CREATE TABLE farms (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(500) NOT NULL,
    size DECIMAL(10, 2) NOT NULL COMMENT 'Size in hectares',
    crop_types TEXT NULL COMMENT 'JSON array of crop types',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USER SETTINGS
-- =====================================================

CREATE TABLE user_settings (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    email_alerts BOOLEAN DEFAULT TRUE,
    language VARCHAR(10) DEFAULT 'en',
    theme VARCHAR(20) DEFAULT 'light',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_settings (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- FCM TOKENS
-- =====================================================

CREATE TABLE fcm_tokens (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    device_info VARCHAR(255) NULL,
    active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ROBOT STATUS
-- =====================================================

CREATE TABLE robot_status (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    status ENUM('active', 'idle', 'charging', 'maintenance', 'offline') DEFAULT 'offline',
    battery_level INT UNSIGNED DEFAULT 0 COMMENT 'Battery percentage 0-100',
    herbicide_level INT UNSIGNED DEFAULT 0 COMMENT 'Herbicide tank level 0-100',
    latitude DECIMAL(10, 8) DEFAULT 0,
    longitude DECIMAL(11, 8) DEFAULT 0,
    speed DECIMAL(5, 2) DEFAULT 0 COMMENT 'Speed in km/h',
    heading DECIMAL(5, 2) DEFAULT 0 COMMENT 'Direction in degrees',
    activity VARCHAR(100) NULL COMMENT 'Current activity description',
    area_covered_today DECIMAL(10, 2) DEFAULT 0 COMMENT 'Area covered today in hectares',
    efficiency DECIMAL(5, 2) DEFAULT 0 COMMENT 'Efficiency percentage',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ROBOT SESSIONS
-- =====================================================

CREATE TABLE robot_sessions (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL COMMENT 'User who owns this session',
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    area_covered DECIMAL(10, 2) DEFAULT 0 COMMENT 'Area in hectares',
    herbicide_used DECIMAL(10, 2) DEFAULT 0 COMMENT 'Herbicide in liters',
    weeds_detected INT UNSIGNED DEFAULT 0,
    battery_start INT UNSIGNED DEFAULT 0,
    battery_end INT UNSIGNED DEFAULT 0,
    status ENUM('active', 'completed', 'interrupted') DEFAULT 'active',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ROBOT ACTIVITY LOG
-- =====================================================

CREATE TABLE robot_activity_log (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL COMMENT 'User who owns this activity',
    action VARCHAR(100) NOT NULL,
    description TEXT NULL,
    status ENUM('started', 'completed', 'failed') DEFAULT 'completed',
    metadata TEXT NULL COMMENT 'JSON data',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- WEED DETECTIONS
-- =====================================================

CREATE TABLE weed_detections (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NULL COMMENT 'User who owns this detection',
    weed_type VARCHAR(100) NOT NULL,
    crop_type VARCHAR(100) NULL,
    confidence DECIMAL(5, 2) DEFAULT 0 COMMENT 'Detection confidence 0-100',
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    image_base64 LONGTEXT NULL COMMENT 'Base64 encoded image data',
    image_mime_type VARCHAR(50) NULL COMMENT 'Image MIME type (image/jpeg, image/png, etc)',
    treated BOOLEAN DEFAULT FALSE,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_weed_type (weed_type),
    INDEX idx_crop_type (crop_type),
    INDEX idx_detected_at (detected_at),
    INDEX idx_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- WEATHER DATA
-- =====================================================

CREATE TABLE weather_data (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    temperature DECIMAL(5, 2) NOT NULL COMMENT 'Temperature in Celsius',
    humidity INT UNSIGNED NOT NULL COMMENT 'Humidity percentage',
    weather_condition VARCHAR(50) NOT NULL COMMENT 'Weather condition (Clear, Cloudy, Rain, etc)',
    wind_speed DECIMAL(5, 2) DEFAULT 0 COMMENT 'Wind speed in km/h',
    wind_direction VARCHAR(10) NULL COMMENT 'Wind direction (N, NE, E, SE, S, SW, W, NW)',
    pressure DECIMAL(7, 2) DEFAULT 0 COMMENT 'Atmospheric pressure in hPa',
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recorded_at (recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- WEATHER FORECAST
-- =====================================================

CREATE TABLE weather_forecast (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    forecast_date DATE NOT NULL,
    temp_high DECIMAL(5, 2) NOT NULL,
    temp_low DECIMAL(5, 2) NOT NULL,
    weather_condition VARCHAR(50) NOT NULL,
    precipitation_chance INT UNSIGNED DEFAULT 0 COMMENT 'Chance of rain 0-100',
    humidity INT UNSIGNED DEFAULT 0,
    wind_speed DECIMAL(5, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_forecast_date (forecast_date),
    INDEX idx_forecast_date (forecast_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SOIL DATA
-- =====================================================

CREATE TABLE soil_data (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    moisture DECIMAL(5, 2) NOT NULL COMMENT 'Soil moisture percentage',
    temperature DECIMAL(5, 2) NOT NULL COMMENT 'Soil temperature in Celsius',
    ph DECIMAL(4, 2) NOT NULL COMMENT 'Soil pH level',
    nitrogen INT UNSIGNED DEFAULT 0 COMMENT 'Nitrogen level (ppm)',
    phosphorus INT UNSIGNED DEFAULT 0 COMMENT 'Phosphorus level (ppm)',
    potassium INT UNSIGNED DEFAULT 0 COMMENT 'Potassium level (ppm)',
    organic_matter DECIMAL(5, 2) DEFAULT 0 COMMENT 'Organic matter percentage',
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recorded_at (recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- ALERTS & NOTIFICATIONS
-- =====================================================

CREATE TABLE alerts (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL COMMENT 'Alert type (battery, fault, maintenance, etc)',
    severity ENUM('info', 'warning', 'critical') DEFAULT 'info',
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    metadata TEXT NULL COMMENT 'JSON data',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    INDEX idx_severity (severity),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- USER IMAGES (Gallery)
-- =====================================================

CREATE TABLE user_images (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    title VARCHAR(255) NULL,
    description TEXT NULL,
    image_base64 LONGTEXT NOT NULL COMMENT 'Base64 encoded image data',
    image_mime_type VARCHAR(50) NOT NULL COMMENT 'Image MIME type (image/jpeg, image/png, etc)',
    category VARCHAR(50) NULL COMMENT 'Image category (avatar, gallery, weed, etc)',
    metadata TEXT NULL COMMENT 'JSON metadata',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- CHAT HISTORY (Assistant)
-- =====================================================

CREATE TABLE chat_history (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    message TEXT NOT NULL,
    is_user BOOLEAN DEFAULT TRUE COMMENT 'TRUE for user messages, FALSE for bot responses',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT SAMPLE DATA
-- =====================================================

-- Demo users (passwords: admin123 and user1234)
-- Note: Run this PHP to generate a new hash: php -r "echo password_hash('user1234', PASSWORD_BCRYPT);"
INSERT INTO users (name, email, password, avatar) VALUES 
('Admin User', 'admin@weedx.com', '$2y$12$gk6.SpklOUO8Ort6z0oLUuxrhkT2fNyzKwEZhwq5lOEwDEx1d1GaW', NULL),
('Umer Sani', 'iamumersani@gmail.com', '$2y$12$a.87atGh7/avc3KQriY9CedH8UNcqgOfXBQk.H/Q5ZJ8ecCWVe4Ou', NULL);

-- Demo farm and user settings for Umer Sani
INSERT INTO farms (user_id, name, location, size, crop_types) VALUES
(2, 'Umer Farm', 'Lahore, Pakistan', 4.25, '["Wheat","Corn"]');

INSERT INTO user_settings (user_id, notifications_enabled, email_alerts, language, theme) VALUES
(2, TRUE, TRUE, 'en', 'dark');

-- Sample robot status
INSERT INTO robot_status (status, battery_level, herbicide_level, latitude, longitude, speed, activity, area_covered_today, efficiency) VALUES
('active', 85, 70, 31.5204, 74.3587, 3.5, 'Scanning field sector A', 12.5, 87.5);

-- Sample alerts
INSERT INTO alerts (type, severity, message) VALUES
('battery', 'warning', 'Robot battery at 85%. Consider charging soon.'),
('maintenance', 'info', 'Regular maintenance due in 3 days'),
('detection', 'info', '47 weeds detected today in wheat field');

-- Sample weather data
INSERT INTO weather_data (temperature, humidity, weather_condition, wind_speed, wind_direction, pressure) VALUES
(25.5, 60, 'Clear', 10.5, 'NE', 1013.0);

-- Sample weather forecast (next 7 days)
INSERT INTO weather_forecast (forecast_date, temp_high, temp_low, weather_condition, precipitation_chance, humidity, wind_speed) VALUES
(CURDATE(), 28, 18, 'Sunny', 10, 55, 12),
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), 27, 17, 'Partly Cloudy', 20, 60, 15),
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), 26, 19, 'Cloudy', 40, 65, 18),
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), 24, 18, 'Rain', 80, 75, 20),
(DATE_ADD(CURDATE(), INTERVAL 4 DAY), 25, 17, 'Partly Cloudy', 30, 60, 14),
(DATE_ADD(CURDATE(), INTERVAL 5 DAY), 27, 18, 'Sunny', 10, 55, 10),
(DATE_ADD(CURDATE(), INTERVAL 6 DAY), 29, 19, 'Sunny', 5, 50, 8);

-- Sample soil data
INSERT INTO soil_data (moisture, temperature, ph, nitrogen, phosphorus, potassium, organic_matter) VALUES
(45.0, 22.0, 6.5, 50, 30, 40, 3.5);

-- Sample weed detections for admin and Umer Sani
INSERT INTO weed_detections (user_id, weed_type, crop_type, confidence, latitude, longitude, image_base64, image_mime_type, treated, detected_at) VALUES
(1, 'Broadleaf Weed', 'Wheat', 92.5, 31.5204, 74.3587, NULL, NULL, FALSE, NOW()),
(1, 'Grass Weed', 'Wheat', 88.3, 31.5210, 74.3590, NULL, NULL, TRUE, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'Broadleaf Weed', 'Corn', 95.1, 31.5198, 74.3580, NULL, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'Sedge', 'Wheat', 79.8, 31.5215, 74.3595, NULL, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 'Daisy Weed', 'Wheat', 91.2, 31.5208, 74.3592, 'RHVtbXkgZGV0ZWN0aW9uIGltYWdl', 'image/jpeg', FALSE, NOW()),
(2, 'Crabgrass', 'Corn', 84.4, 31.5212, 74.3583, 'RHVtbXkgZGV0ZWN0aW9uIGltYWdl', 'image/png', TRUE, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- Sample robot sessions for Umer Sani
INSERT INTO robot_sessions (user_id, start_time, end_time, area_covered, herbicide_used, weeds_detected, battery_start, battery_end, status, notes) VALUES
(2, DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 3.20, 12.5, 18, 95, 60, 'completed', 'Completed morning weeding pass.'),
(2, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), 1.10, 4.0, 8, 100, 85, 'completed', 'Afternoon scouting of field perimeter.');

-- Sample robot activity log for Umer Sani
INSERT INTO robot_activity_log (user_id, action, description, status, metadata) VALUES
(2, 'Start Session', 'Robot started field scanning operation', 'completed', '{"session":"morning-pass"}'),
(2, 'Weed Detection', 'Detected daisy weed in north field', 'completed', '{"count":18}'),
(2, 'Herbicide Application', 'Applied herbicide to detected weeds', 'completed', '{"volume_l":12.5}'),
(2, 'Battery Check', 'Battery level checked: 60%', 'completed', '{"level":60}');

-- Sample user images for Umer Sani
INSERT INTO user_images (user_id, title, description, image_base64, image_mime_type, category, metadata) VALUES
(2, 'Field Scan', 'Drone view of wheat field', 'RHVtbXkgZmlsZSBpbWFnZQ==', 'image/jpeg', 'gallery', '{"source":"drone"}'),
(2, 'Detected Weed', 'Closeup of detected crabgrass', 'RHVtbXkgZW5jb2RlZCBpbW1hZ2U=', 'image/png', 'weed', '{"confidence":84.4}');

-- Sample chat history for Umer Sani
INSERT INTO chat_history (user_id, message, is_user) VALUES
(2, 'Hello WeedX, show me today''s weed detections.', TRUE),
(2, 'Here are 2 new detections in your wheat and corn fields.', FALSE);

COMMIT;
