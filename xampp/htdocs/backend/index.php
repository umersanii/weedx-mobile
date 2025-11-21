<?php
/**
 * WeedX Backend API Router
 * Entry point for all API requests
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');

// Handle preflight OPTIONS requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/utils/response.php';
require_once __DIR__ . '/utils/auth.php';

// Get request path
$request = $_GET['request'] ?? '';
$method = $_SERVER['REQUEST_METHOD'];

// Remove trailing slash
$request = rtrim($request, '/');

// Route requests
switch (true) {
    // Auth endpoints
    case $request === 'auth/login' && $method === 'POST':
        require __DIR__ . '/api/auth/login.php';
        break;
    
    case $request === 'auth/logout' && $method === 'POST':
        require __DIR__ . '/api/auth/logout.php';
        break;
    
    case $request === 'auth/refresh' && $method === 'POST':
        require __DIR__ . '/api/auth/refresh.php';
        break;
    
    // Landing/Dashboard
    case $request === 'landing' && $method === 'GET':
        require __DIR__ . '/api/landing.php';
        break;
    
    case $request === 'robot/status' && $method === 'GET':
        require __DIR__ . '/api/robot/status.php';
        break;
    
    case $request === 'summary/today' && $method === 'GET':
        require __DIR__ . '/api/summary/today.php';
        break;
    
    case $request === 'alerts/recent' && $method === 'GET':
        require __DIR__ . '/api/alerts/recent.php';
        break;
    
    // Environment
    case $request === 'environment' && $method === 'GET':
        require __DIR__ . '/api/environment/index.php';
        break;
    
    case $request === 'environment/weather/current' && $method === 'GET':
        require __DIR__ . '/api/environment/weather/current.php';
        break;
    
    case $request === 'environment/weather/forecast' && $method === 'GET':
        require __DIR__ . '/api/environment/weather/forecast.php';
        break;
    
    case $request === 'environment/soil' && $method === 'GET':
        require __DIR__ . '/api/environment/soil.php';
        break;
    
    case $request === 'environment/recommendations/today' && $method === 'GET':
        require __DIR__ . '/api/environment/recommendations/today.php';
        break;
    
    // Monitoring
    case $request === 'monitoring' && $method === 'GET':
        require __DIR__ . '/api/monitoring/index.php';
        break;
    
    case $request === 'monitoring/metrics' && $method === 'GET':
        require __DIR__ . '/api/monitoring/metrics.php';
        break;
    
    case $request === 'monitoring/activity' && $method === 'GET':
        require __DIR__ . '/api/monitoring/activity.php';
        break;
    
    case $request === 'monitoring/location' && $method === 'GET':
        require __DIR__ . '/api/monitoring/location.php';
        break;
    
    // Weed Logs
    case $request === 'weed-logs' && $method === 'GET':
        require __DIR__ . '/api/weed-logs/index.php';
        break;
    
    case $request === 'weed-logs/summary' && $method === 'GET':
        require __DIR__ . '/api/weed-logs/summary.php';
        break;
    
    case $request === 'weed-logs/detections' && $method === 'GET':
        require __DIR__ . '/api/weed-logs/detections.php';
        break;
    
    // Reports
    case $request === 'reports' && $method === 'GET':
        require __DIR__ . '/api/reports/index.php';
        break;
    
    case $request === 'reports/widgets' && $method === 'GET':
        require __DIR__ . '/api/reports/widgets.php';
        break;
    
    case $request === 'reports/weed-trend' && $method === 'GET':
        require __DIR__ . '/api/reports/weed-trend.php';
        break;
    
    case $request === 'reports/weed-distribution' && $method === 'GET':
        require __DIR__ . '/api/reports/weed-distribution.php';
        break;
    
    case $request === 'reports/export' && $method === 'GET':
        require __DIR__ . '/api/reports/export.php';
        break;
    
    // Gallery
    case $request === 'gallery' && $method === 'GET':
        require __DIR__ . '/api/gallery/list.php';
        break;
    
    case $request === 'gallery' && $method === 'POST':
        require __DIR__ . '/api/gallery/upload.php';
        break;
    
    case preg_match('/^gallery\/(\d+)$/', $request, $matches) && $method === 'GET':
        $_GET['id'] = $matches[1];
        require __DIR__ . '/api/gallery/view.php';
        break;
    
    case preg_match('/^gallery\/(\d+)$/', $request, $matches) && $method === 'DELETE':
        $_GET['id'] = $matches[1];
        require __DIR__ . '/api/gallery/delete.php';
        break;
    
    // Profile
    case $request === 'profile' && $method === 'GET':
        require __DIR__ . '/api/profile/get.php';
        break;
    
    case $request === 'profile' && $method === 'PUT':
        require __DIR__ . '/api/profile/update.php';
        break;
    
    case $request === 'profile/avatar' && $method === 'PATCH':
        require __DIR__ . '/api/profile/avatar.php';
        break;
    
    case $request === 'profile/farm' && $method === 'GET':
        require __DIR__ . '/api/profile/farm/get.php';
        break;
    
    case $request === 'profile/farm' && $method === 'PUT':
        require __DIR__ . '/api/profile/farm/update.php';
        break;
    
    case $request === 'profile/settings' && $method === 'GET':
        require __DIR__ . '/api/profile/settings/get.php';
        break;
    
    case $request === 'profile/settings' && $method === 'PUT':
        require __DIR__ . '/api/profile/settings/update.php';
        break;
    
    // Assistant
    case $request === 'assistant/query' && $method === 'POST':
        require __DIR__ . '/api/assistant/query.php';
        break;
    
    case $request === 'assistant/history' && $method === 'GET':
        require __DIR__ . '/api/assistant/history.php';
        break;
    
    // 404 Not Found
    default:
        Response::error('Endpoint not found', 404);
        break;
}
