<?php
/**
 * Profile Avatar Update Endpoint
 * PATCH /api/profile/avatar
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/image_helper.php';

$tokenData = Auth::validateToken();

// Check if file was uploaded
if (!isset($_FILES['avatar'])) {
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
    Response::success(['avatar_url' => $result['full_url']], 'Avatar updated successfully');
} else {
    Response::error('Failed to update avatar', 500);
}
