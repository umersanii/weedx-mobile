<?php
/**
 * Gallery List Endpoint
 * GET /api/gallery
 * 
 * Returns gallery images with full URLs for file-based storage
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/gallery', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/gallery', $tokenData['user_id'] ?? null, true);
$database = new Database();
$db = $database->getConnection();

try {
    $limit = $_GET['limit'] ?? 50;
    $offset = $_GET['offset'] ?? 0;
    $startDate = $_GET['start_date'] ?? null;
    $endDate = $_GET['end_date'] ?? null;
    
    // Get images that have a file path stored
    // Get all detections (with or without images)
    $query = "
        SELECT id, weed_type, confidence, latitude, longitude, 
               image_base64, image_mime_type, image_path, detected_at, crop_type 
        FROM weed_detections 
        WHERE user_id = :user_id
    ";
    
    // Add date filtering if provided
    if ($startDate) {
        $query .= " AND DATE(detected_at) >= :start_date";
    }
    if ($endDate) {
        $query .= " AND DATE(detected_at) <= :end_date";
    }
    
    $query .= " ORDER BY detected_at DESC LIMIT :limit OFFSET :offset";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
    
    if ($startDate) {
        $stmt->bindParam(':start_date', $startDate, PDO::PARAM_STR);
    }
    if ($endDate) {
        $stmt->bindParam(':end_date', $endDate, PDO::PARAM_STR);
    }
    
    $stmt->execute();
    
    $images = $stmt->fetchAll();
    
    // Build base URL for image paths - use forwarded headers if behind proxy
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'] ?? $_SERVER['SERVER_NAME'] ?? 'localhost:8000';
    
    // Check for forwarded headers (when behind proxy/Tailscale)
    if (!empty($_SERVER['HTTP_X_FORWARDED_HOST'])) {
        $host = $_SERVER['HTTP_X_FORWARDED_HOST'];
    }
    if (!empty($_SERVER['HTTP_X_FORWARDED_PROTO'])) {
        $protocol = $_SERVER['HTTP_X_FORWARDED_PROTO'];
    }
    
    // Get the script's directory to determine the base path
    $scriptPath = dirname($_SERVER['SCRIPT_NAME']);
    // Remove /api/gallery or similar from the path to get backend root
    $backendRoot = preg_replace('/\/api.*$/', '', $scriptPath);
    
    $baseUrl = $protocol . '://' . $host . $backendRoot;
    
    // Log the base URL for debugging
    Logger::logRequest('/api/gallery', 'GET', ['baseUrl' => $baseUrl, 'host' => $host, 'backendRoot' => $backendRoot]);
    
    $response = array_map(function($image) use ($baseUrl) {
        // Determine image URL: prefer base64, fallback to file path
        $imageUrl = null;
        if (!empty($image['image_base64'])) {
            $mime = $image['image_mime_type'] ?? 'image/jpeg';
            $imageUrl = 'data:' . $mime . ';base64,' . $image['image_base64'];
        } elseif (!empty($image['image_path'])) {
            // Make sure to return full URL for file paths
            $path = $image['image_path'];
            if (strpos($path, 'http') !== 0 && strpos($path, 'data:') !== 0) {
                $imageUrl = $baseUrl . $path;
            } else {
                $imageUrl = $path;
            }
        }
        
        return [
            'id' => (int)$image['id'],
            'url' => $imageUrl,
            'thumbnail_url' => $imageUrl, // Same as url for now
            'image_url' => $imageUrl, // Keep for backward compatibility
            'has_image' => $imageUrl !== null,
            'weed_type' => $image['weed_type'],
            'crop_type' => $image['crop_type'],
            'confidence' => (float)$image['confidence'],
            'location' => [
                'latitude' => (float)$image['latitude'],
                'longitude' => (float)$image['longitude']
            ],
            'captured_at' => $image['detected_at']
        ];
    }, $images);
    
    Logger::logSuccess('/api/gallery', 'Fetched ' . count($response) . ' gallery images');
    Response::success($response);
} catch (Exception $e) {
    Logger::logError('/api/gallery', $e->getMessage(), 500);
    Response::error('Failed to fetch gallery: ' . $e->getMessage(), 500);
}
