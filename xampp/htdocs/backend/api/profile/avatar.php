<?php
/**
 * Profile Avatar Update Endpoint
 * PATCH /api/profile/avatar
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/profile/avatar', 'PATCH');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/profile/avatar', $tokenData['userId'] ?? null, true);

// Check if file was uploaded
if (!isset($_FILES['avatar'])) {
    Logger::logError('/api/profile/avatar', 'No avatar file provided', 400);
    Response::error('No avatar file provided', 400);
}

// Generate custom filename with user ID
$userId = $tokenData['userId'];
$extension = pathinfo($_FILES['avatar']['name'], PATHINFO_EXTENSION);
$customFilename = 'avatar_' . $userId . '_' . time() . '.' . $extension;

// Use ImageHelper to save the avatar
$result = ImageHelper::saveUploadedImage(
    $_FILES['avatar'],
    ImageHelper::CATEGORY_AVATAR,
    $customFilename,
    2 // Max 2MB for avatars
);

if (!$result['success']) {
    Logger::logError('/api/profile/avatar', $result['error'], 400);
    Response::error($result['error'], 400);
}

// Update database
$database = new Database();
$db = $database->getConnection();

$avatarPath = $result['relative_path'];

$query = "UPDATE users SET avatar = :avatar WHERE id = :id";
$stmt = $db->prepare($query);
$stmt->bindParam(':avatar', $avatarPath);
$stmt->bindParam(':id', $userId);

if ($stmt->execute()) {
    Logger::logSuccess('/api/profile/avatar', 'Avatar updated for user ID: ' . $userId);
    Response::success(['avatar_url' => $result['full_url']], 'Avatar updated successfully');
} else {
    Logger::logError('/api/profile/avatar', 'Failed to update avatar', 500);
    Response::error('Failed to update avatar', 500);
}
