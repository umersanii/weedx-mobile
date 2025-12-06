-- Migration: Add user_id to robot_sessions and robot_activity_log tables
-- Date: 2025-12-08
-- Description: Adds user_id foreign key to robot tables for proper data isolation

USE weedx;

-- =====================================================
-- Step 1: Add user_id column to robot_sessions
-- =====================================================

-- Add the column (nullable first to allow existing rows)
ALTER TABLE robot_sessions 
ADD COLUMN user_id INT UNSIGNED NULL COMMENT 'User who owns this session' AFTER id;

-- If you have existing data, assign it to a default user (adjust user ID as needed)
-- Option A: Assign all existing sessions to user ID 1 (first registered user)
UPDATE robot_sessions SET user_id = 1 WHERE user_id IS NULL;

-- Option B: Delete existing orphaned sessions (if any exist)
-- DELETE FROM robot_sessions WHERE user_id IS NULL;

-- Make the column NOT NULL after populating existing rows
ALTER TABLE robot_sessions 
MODIFY COLUMN user_id INT UNSIGNED NOT NULL;

-- Add foreign key constraint
ALTER TABLE robot_sessions 
ADD CONSTRAINT fk_robot_sessions_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index for performance
ALTER TABLE robot_sessions 
ADD INDEX idx_user_id (user_id);

-- =====================================================
-- Step 2: Add user_id column to robot_activity_log
-- =====================================================

-- Add the column (nullable first to allow existing rows)
ALTER TABLE robot_activity_log 
ADD COLUMN user_id INT UNSIGNED NULL COMMENT 'User who owns this activity' AFTER id;

-- If you have existing data, assign it to a default user (adjust user ID as needed)
-- Option A: Assign all existing activities to user ID 1 (first registered user)
UPDATE robot_activity_log SET user_id = 1 WHERE user_id IS NULL;

-- Option B: Delete existing orphaned activities (if any exist)
-- DELETE FROM robot_activity_log WHERE user_id IS NULL;

-- Make the column NOT NULL after populating existing rows
ALTER TABLE robot_activity_log 
MODIFY COLUMN user_id INT UNSIGNED NOT NULL;

-- Add foreign key constraint
ALTER TABLE robot_activity_log 
ADD CONSTRAINT fk_robot_activity_log_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index for performance
ALTER TABLE robot_activity_log 
ADD INDEX idx_user_id (user_id);

-- =====================================================
-- Verification Queries
-- =====================================================

-- Check the updated table structures
DESCRIBE robot_sessions;
DESCRIBE robot_activity_log;

-- Verify foreign keys were created
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'weedx' 
  AND TABLE_NAME IN ('robot_sessions', 'robot_activity_log')
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Check row counts
SELECT 
    (SELECT COUNT(*) FROM robot_sessions) as sessions_count,
    (SELECT COUNT(*) FROM robot_activity_log) as activities_count,
    (SELECT COUNT(*) FROM users) as users_count;

-- =====================================================
-- Migration Complete
-- =====================================================
