<?php
/**
 * Profile Avatar Update Endpoint
 * PATCH /api/profile/avatar
 */

require_once __DIR__ . '/../../../config/database.php';
require_once __DIR__ . '/../../../utils/response.php';
require_once __DIR__ . '/../../../utils/auth.php';

$tokenData = Auth::validateToken();

// Check if file was uploaded
if (!isset($_FILES['avatar'])) {
    Response::error('No avatar file provided', 400);
}

$file = $_FILES['avatar'];

// Validate file type
$allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
if (!in_array($file['type'], $allowedTypes)) {
    Response::error('Invalid file type. Only JPEG and PNG allowed', 400);
}

// Validate file size (max 2MB)
if ($file['size'] > 2 * 1024 * 1024) {
    Response::error('File too large. Maximum 2MB', 400);
}

// Create uploads directory if not exists
$uploadDir = __DIR__ . '/../../../uploads/avatars/';
if (!file_exists($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

// Generate unique filename
$extension = pathinfo($file['name'], PATHINFO_EXTENSION);
$filename = 'avatar_' . $tokenData['userId'] . '_' . time() . '.' . $extension;
$filepath = $uploadDir . $filename;

// Move uploaded file
if (!move_uploaded_file($file['tmp_name'], $filepath)) {
    Response::error('Failed to upload file', 500);
}

// Update database
$database = new Database();
$db = $database->getConnection();

$avatarPath = '/uploads/avatars/' . $filename;
$userId = $tokenData['userId'];

$query = "UPDATE users SET avatar = :avatar WHERE id = :id";
$stmt = $db->prepare($query);
$stmt->bindParam(':avatar', $avatarPath);
$stmt->bindParam(':id', $userId);

if ($stmt->execute()) {
    Response::success(['avatar_url' => $avatarPath], 'Avatar updated successfully');
} else {
    Response::error('Failed to update avatar', 500);
}
