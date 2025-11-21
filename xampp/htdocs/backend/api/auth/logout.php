<?php
/**
 * Logout Endpoint
 * POST /api/auth/logout
 */

require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

// Validate token
$tokenData = Auth::validateToken();

// In a real app, you might want to blacklist the token
// For now, just return success

Response::success(null, 'Logged out successfully');
