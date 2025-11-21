<?php
/**
 * Assistant Query Endpoint
 * POST /api/assistant/query
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';

$tokenData = Auth::validateToken();
$database = new Database();
$db = $database->getConnection();

$data = json_decode(file_get_contents("php://input"), true);

Response::validateRequired($data, ['query']);

$query = $data['query'];
$userId = $tokenData['userId'];

try {
    // Save user query to history
    $insertQuery = "INSERT INTO chat_history (user_id, message, is_user, created_at) 
                    VALUES (:user_id, :message, 1, NOW())";
    $stmt = $db->prepare($insertQuery);
    $stmt->bindParam(':user_id', $userId);
    $stmt->bindParam(':message', $query);
    $stmt->execute();
    
    // TODO: Integrate with actual AI/LLM API (OpenAI, Anthropic, etc.)
    // For now, provide simple keyword-based responses
    
    $response = generateResponse($query);
    
    // Save bot response to history
    $insertResponse = "INSERT INTO chat_history (user_id, message, is_user, created_at) 
                       VALUES (:user_id, :message, 0, NOW())";
    $respStmt = $db->prepare($insertResponse);
    $respStmt->bindParam(':user_id', $userId);
    $respStmt->bindParam(':message', $response);
    $respStmt->execute();
    
    Response::success([
        'query' => $query,
        'response' => $response,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
} catch (Exception $e) {
    Response::error('Failed to process query: ' . $e->getMessage(), 500);
}

function generateResponse($query) {
    $query = strtolower($query);
    
    if (strpos($query, 'weed') !== false) {
        return "Based on your recent data, I've detected several common weeds in your field. The most prevalent are broadleaf weeds, which are best treated early in the morning. Would you like specific treatment recommendations?";
    }
    
    if (strpos($query, 'weather') !== false) {
        return "Current weather conditions are favorable for farming operations. Temperature is optimal for herbicide application. Check the Weather tab for detailed forecasts.";
    }
    
    if (strpos($query, 'robot') !== false || strpos($query, 'status') !== false) {
        return "Your robot is currently operational with 85% battery. It has covered 12.5 hectares today and detected 47 weeds. All systems are functioning normally.";
    }
    
    if (strpos($query, 'soil') !== false) {
        return "Your soil moisture levels are optimal for weeding operations. pH is within normal range. Consider nitrogen supplementation in the northern section of your farm.";
    }
    
    if (strpos($query, 'crop') !== false) {
        return "Your crops are in good health. Monitor for early signs of stress in hot weather. Ensure adequate watering during peak growth periods.";
    }
    
    return "I'm here to help you with farming advice! You can ask me about weed management, weather conditions, robot status, soil health, or crop recommendations. What would you like to know?";
}
