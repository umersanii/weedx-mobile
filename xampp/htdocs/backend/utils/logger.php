<?php
/**
 * Logger Utility
 * Logs API endpoint hits with timestamps, methods, and request details
 */

class Logger {
    private static $logFile = null;
    private static $logDir = null;
    
    /**
     * Initialize log directory and file
     */
    private static function init() {
        if (self::$logDir === null) {
            self::$logDir = __DIR__ . '/../logs';
            if (!is_dir(self::$logDir)) {
                mkdir(self::$logDir, 0755, true);
            }
        }
        
        if (self::$logFile === null) {
            self::$logFile = self::$logDir . '/api_' . date('Y-m-d') . '.log';
        }
    }
    
    /**
     * Log an API request
     * 
     * @param string $endpoint The API endpoint (e.g., '/api/auth/login')
     * @param string $method HTTP method (GET, POST, etc.)
     * @param array $extra Extra data to log (optional)
     */
    public static function logRequest($endpoint, $method = null, $extra = []) {
        self::init();
        
        $method = $method ?? $_SERVER['REQUEST_METHOD'];
        $timestamp = date('Y-m-d H:i:s');
        $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        $userAgent = substr($_SERVER['HTTP_USER_AGENT'] ?? 'unknown', 0, 100);
        
        $logEntry = sprintf(
            "[%s] %s %s | IP: %s | UA: %s",
            $timestamp,
            str_pad($method, 6),
            $endpoint,
            $ip,
            $userAgent
        );
        
        if (!empty($extra)) {
            $logEntry .= " | Extra: " . json_encode($extra);
        }
        
        $logEntry .= PHP_EOL;
        
        file_put_contents(self::$logFile, $logEntry, FILE_APPEND | LOCK_EX);
        
        // Also log to error_log for real-time viewing in terminal
        error_log("🔹 API HIT: {$method} {$endpoint}");
    }
    
    /**
     * Log successful response
     */
    public static function logSuccess($endpoint, $message = 'Success') {
        self::init();
        
        $timestamp = date('Y-m-d H:i:s');
        $logEntry = sprintf(
            "[%s] ✅ SUCCESS %s | %s\n",
            $timestamp,
            $endpoint,
            $message
        );
        
        file_put_contents(self::$logFile, $logEntry, FILE_APPEND | LOCK_EX);
        error_log("✅ API SUCCESS: {$endpoint} - {$message}");
    }
    
    /**
     * Log error response
     */
    public static function logError($endpoint, $message, $statusCode = 400) {
        self::init();
        
        $timestamp = date('Y-m-d H:i:s');
        $logEntry = sprintf(
            "[%s] ❌ ERROR %s | HTTP %d | %s\n",
            $timestamp,
            $endpoint,
            $statusCode,
            $message
        );
        
        file_put_contents(self::$logFile, $logEntry, FILE_APPEND | LOCK_EX);
        error_log("❌ API ERROR: {$endpoint} - HTTP {$statusCode} - {$message}");
    }
    
    /**
     * Log authentication events
     */
    public static function logAuth($endpoint, $userId = null, $success = true) {
        self::init();
        
        $timestamp = date('Y-m-d H:i:s');
        $status = $success ? '🔓 AUTH OK' : '🔒 AUTH FAIL';
        $userInfo = $userId ? "User: {$userId}" : "User: unknown";
        
        $logEntry = sprintf(
            "[%s] %s %s | %s\n",
            $timestamp,
            $status,
            $endpoint,
            $userInfo
        );
        
        file_put_contents(self::$logFile, $logEntry, FILE_APPEND | LOCK_EX);
        error_log("{$status}: {$endpoint} - {$userInfo}");
    }
    
    /**
     * Get recent logs
     */
    public static function getRecentLogs($lines = 100) {
        self::init();
        
        if (!file_exists(self::$logFile)) {
            return [];
        }
        
        $logs = file(self::$logFile);
        return array_slice($logs, -$lines);
    }
}
