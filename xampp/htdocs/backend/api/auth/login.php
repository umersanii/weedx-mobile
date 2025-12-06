<?php
/**
 * Login Endpoint
 * POST /api/auth/login
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../config/firebase.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/auth/login', 'POST');

// Get POST data
$data = json_decode(file_get_contents("php://input"), true);

// Validate required fields
Response::validateRequired($data, ['email', 'password', 'firebaseToken']);

$email = $data['email'];
$password = $data['password'];
$firebaseToken = $data['firebaseToken'] ?? null;

// TODO: Verify Firebase token when Firebase is configured
// For now, skip Firebase verification for testing
// $firebase = new Firebase();
// if (!$firebase->verifyIdToken($firebaseToken)) {
//     Response::error('Invalid Firebase token', 401);
// }

// Connect to database
$database = new Database();
$db = $database->getConnection();

// Check if user exists
$query = "SELECT * FROM users WHERE email = :email LIMIT 1";
$stmt = $db->prepare($query);
$stmt->bindParam(':email', $email);
$stmt->execute();

$user = $stmt->fetch();

if (!$user) {
    Response::error('Invalid credentials', 401);
}

// Verify password
if (!Auth::verifyPassword($password, $user['password'])) {
    Response::error('Invalid credentials', 401);
}

// Generate JWT token
$token = Auth::generateToken($user['id'], $user['email']);

// Update last login
$updateQuery = "UPDATE users SET last_login = NOW() WHERE id = :id";
$updateStmt = $db->prepare($updateQuery);
$updateStmt->bindParam(':id', $user['id']);
$updateStmt->execute();

// Return success response
Logger::logAuth('/api/auth/login', $user['id'], true);
Logger::logSuccess('/api/auth/login', 'User logged in: ' . $user['email']);
Response::success([
    'token' => $token,
    'userId' => (string)$user['id'],
    'email' => $user['email']
], 'Login successful');
