<?php
/**
 * Export Report Endpoint
 * GET /api/reports/export?format=csv
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/reports/export', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/reports/export', $tokenData['userId'] ?? null, true);

$format = $_GET['format'] ?? 'csv';

if (!in_array($format, ['csv'])) {
    Response::error('Invalid format. Only CSV is supported', 400);
}

$database = new Database();
$db = $database->getConnection();

try {
    // Fetch all weed detections for the user
    $query = "
        SELECT 
            id,
            weed_type,
            crop_type,
            confidence,
            latitude,
            longitude,
            treatment_action,
            detected_at
        FROM weed_detections 
        WHERE user_id = :user_id
        ORDER BY detected_at DESC
    ";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $stmt->execute();
    $detections = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Generate CSV content
    $csvData = "ID,Weed Type,Crop Type,Confidence,Latitude,Longitude,Treatment Action,Detected At\n";
    
    foreach ($detections as $detection) {
        $csvData .= sprintf(
            "%d,%s,%s,%.2f%%,%.6f,%.6f,%s,%s\n",
            $detection['id'],
            $detection['weed_type'] ?? 'Unknown',
            $detection['crop_type'] ?? 'Unknown',
            $detection['confidence'] ?? 0,
            $detection['latitude'] ?? 0,
            $detection['longitude'] ?? 0,
            $detection['treatment_action'] ?? 'None',
            $detection['detected_at'] ?? ''
        );
    }
    
    // Return base64 encoded CSV
    $filename = 'weedx_report_' . date('Y-m-d_His') . '.csv';
    
    Logger::logSuccess('/api/reports/export', 'CSV generated: ' . $filename);
    Response::success([
        'format' => 'csv',
        'filename' => $filename,
        'download_url' => 'data:text/csv;base64,' . base64_encode($csvData),
        'generated_at' => date('Y-m-d H:i:s')
    ], 'Report generated successfully');
    
} catch (PDOException $e) {
    Logger::logError('/api/reports/export', 'Database error: ' . $e->getMessage());
    Response::error('Failed to generate report: ' . $e->getMessage(), 500);
}
