<?php
/**
 * User Registration Endpoint
 * POST /api/auth/register
 * 
 * Creates a new user account with associated farm and settings
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error('Method not allowed', 405);
}

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

// Validate required fields
$requiredFields = ['name', 'email', 'password', 'farmName', 'farmLocation', 'farmSize'];
foreach ($requiredFields as $field) {
    if (empty($input[$field])) {
        Response::error("Missing required field: $field", 400);
    }
}

// Validate email format
if (!filter_var($input['email'], FILTER_VALIDATE_EMAIL)) {
    Response::error('Invalid email format', 400);
}

// Validate password length
if (strlen($input['password']) < 6) {
    Response::error('Password must be at least 6 characters', 400);
}

// Validate farm size
if (!is_numeric($input['farmSize']) || $input['farmSize'] <= 0) {
    Response::error('Invalid farm size', 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // Check if email already exists
    $checkQuery = "SELECT id FROM users WHERE email = :email";
    $checkStmt = $db->prepare($checkQuery);
    $checkStmt->bindParam(':email', $input['email']);
    $checkStmt->execute();
    
    if ($checkStmt->fetch()) {
        Response::error('Email already registered', 409);
    }
    
    // Begin transaction
    $db->beginTransaction();
    
    // Hash password
    $hashedPassword = password_hash($input['password'], PASSWORD_BCRYPT);
    
    // Insert user
    $userQuery = "INSERT INTO users (name, email, password, phone, created_at) 
                  VALUES (:name, :email, :password, :phone, NOW())";
    $userStmt = $db->prepare($userQuery);
    $userStmt->bindParam(':name', $input['name']);
    $userStmt->bindParam(':email', $input['email']);
    $userStmt->bindParam(':password', $hashedPassword);
    $userStmt->bindValue(':phone', $input['phone'] ?? null);
    $userStmt->execute();
    
    $userId = $db->lastInsertId();
    
    // Prepare crop types as JSON
    $cropTypes = null;
    if (!empty($input['cropTypes']) && is_array($input['cropTypes'])) {
        $cropTypes = json_encode($input['cropTypes']);
    }
    
    // Insert farm
    $farmQuery = "INSERT INTO farms (user_id, name, location, size, crop_types, created_at) 
                  VALUES (:user_id, :name, :location, :size, :crop_types, NOW())";
    $farmStmt = $db->prepare($farmQuery);
    $farmStmt->bindParam(':user_id', $userId);
    $farmStmt->bindParam(':name', $input['farmName']);
    $farmStmt->bindParam(':location', $input['farmLocation']);
    $farmStmt->bindParam(':size', $input['farmSize']);
    $farmStmt->bindParam(':crop_types', $cropTypes);
    $farmStmt->execute();
    
    // Insert user settings
    $notificationsEnabled = isset($input['notificationsEnabled']) ? ($input['notificationsEnabled'] ? 1 : 0) : 1;
    $emailAlerts = isset($input['emailAlerts']) ? ($input['emailAlerts'] ? 1 : 0) : 1;
    $language = $input['language'] ?? 'en';
    $theme = $input['theme'] ?? 'light';
    
    $settingsQuery = "INSERT INTO user_settings (user_id, notifications_enabled, email_alerts, language, theme, created_at) 
                      VALUES (:user_id, :notifications_enabled, :email_alerts, :language, :theme, NOW())";
    $settingsStmt = $db->prepare($settingsQuery);
    $settingsStmt->bindParam(':user_id', $userId);
    $settingsStmt->bindParam(':notifications_enabled', $notificationsEnabled);
    $settingsStmt->bindParam(':email_alerts', $emailAlerts);
    $settingsStmt->bindParam(':language', $language);
    $settingsStmt->bindParam(':theme', $theme);
    $settingsStmt->execute();
    
    // Commit transaction
    $db->commit();
    
    // Generate JWT token using Auth class
    $token = Auth::generateToken($userId, $input['email']);
    
    Logger::logSuccess('/api/auth/register', 'User registered: ' . $input['email']);
    
    // Return success response
    Response::success([
        'token' => $token,
        'userId' => (string)$userId,
        'email' => $input['email'],
        'message' => 'Registration successful'
    ], 'User registered successfully');
    
} catch (Exception $e) {
    // Rollback on error
    if ($db->inTransaction()) {
        $db->rollBack();
    }
    Response::error('Registration failed: ' . $e->getMessage(), 500);
}
