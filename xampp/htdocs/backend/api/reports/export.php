<?php
/**
 * Export Report Endpoint
 * GET /api/reports/export?format=csv|pdf
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/reports/export', 'GET');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/reports/export', $tokenData['userId'] ?? null, true);

$format = $_GET['format'] ?? 'csv';

if (!in_array($format, ['csv', 'pdf'])) {
    Response::error('Invalid format. Use csv or pdf', 400);
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
            treated,
            detected_at
        FROM weed_detections 
        WHERE user_id = :user_id
        ORDER BY detected_at DESC
    ";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(':user_id', $tokenData['userId'], PDO::PARAM_INT);
    $stmt->execute();
    $detections = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if ($format === 'csv') {
        // Generate CSV content
        $csvData = "ID,Weed Type,Crop Type,Confidence,Latitude,Longitude,Treated,Detected At\n";
        
        foreach ($detections as $detection) {
            $csvData .= sprintf(
                "%d,%s,%s,%.2f%%,%.6f,%.6f,%s,%s\n",
                $detection['id'],
                $detection['weed_type'] ?? 'Unknown',
                $detection['crop_type'] ?? 'Unknown',
                $detection['confidence'] ?? 0,
                $detection['latitude'] ?? 0,
                $detection['longitude'] ?? 0,
                $detection['treated'] ? 'Yes' : 'No',
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
        
    } else {
        // Generate PDF content (simple HTML-based PDF)
        $html = '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1 { color: #2d5016; text-align: center; }
        .info { margin: 20px 0; text-align: center; color: #666; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th { background-color: #2d5016; color: white; padding: 10px; text-align: left; }
        td { padding: 8px; border-bottom: 1px solid #ddd; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        .footer { margin-top: 30px; text-align: center; color: #999; font-size: 12px; }
    </style>
</head>
<body>
    <h1>WeedX Detection Report</h1>
    <div class="info">
        <p>Generated: ' . date('F d, Y H:i:s') . '</p>
        <p>Total Detections: ' . count($detections) . '</p>
    </div>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Weed Type</th>
                <th>Crop Type</th>
                <th>Confidence</th>
                <th>Location</th>
                <th>Treated</th>
                <th>Detected At</th>
            </tr>
        </thead>
        <tbody>';
        
        foreach ($detections as $detection) {
            $html .= sprintf(
                '<tr>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%.2f%%</td>
                    <td>%.6f, %.6f</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>',
                $detection['id'],
                htmlspecialchars($detection['weed_type'] ?? 'Unknown'),
                htmlspecialchars($detection['crop_type'] ?? 'Unknown'),
                $detection['confidence'] ?? 0,
                $detection['latitude'] ?? 0,
                $detection['longitude'] ?? 0,
                $detection['treated'] ? 'Yes' : 'No',
                $detection['detected_at'] ?? ''
            );
        }
        
        $html .= '
        </tbody>
    </table>
    <div class="footer">
        <p>WeedX - Precision Farming Report</p>
    </div>
</body>
</html>';
        
        // For basic PDF support, we'll use DomPDF or return HTML that can be converted
        // Since we don't have PDF libraries installed, return HTML as PDF placeholder
        $filename = 'weedx_report_' . date('Y-m-d_His') . '.pdf';
        
        // Convert HTML to base64 (client will need to handle PDF generation)
        // For now, return as HTML wrapped in PDF structure
        Logger::logSuccess('/api/reports/export', 'PDF generated: ' . $filename);
        Response::success([
            'format' => 'pdf',
            'filename' => $filename,
            'download_url' => 'data:text/html;base64,' . base64_encode($html),
            'generated_at' => date('Y-m-d H:i:s')
        ], 'Report generated successfully (HTML format - convert to PDF in app)');
    }
    
} catch (PDOException $e) {
    Logger::logError('/api/reports/export', 'Database error: ' . $e->getMessage());
    Response::error('Failed to generate report: ' . $e->getMessage(), 500);
}
