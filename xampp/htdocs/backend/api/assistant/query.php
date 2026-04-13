<?php
/**
 * Assistant Query Endpoint
 * POST /api/assistant/query
 */

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../utils/response.php';
require_once __DIR__ . '/../../utils/auth.php';
require_once __DIR__ . '/../../utils/logger.php';

Logger::logRequest('/api/assistant/query', 'POST');

$tokenData = Auth::validateToken();
Logger::logAuth('/api/assistant/query', $tokenData['userId'] ?? null, true);
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
    
    $response = generateResponse($query);
    
    // Save bot response to history
    $insertResponse = "INSERT INTO chat_history (user_id, message, is_user, created_at) 
                       VALUES (:user_id, :message, 0, NOW())";
    $respStmt = $db->prepare($insertResponse);
    $respStmt->bindParam(':user_id', $userId);
    $respStmt->bindParam(':message', $response);
    $respStmt->execute();
    
    Logger::logSuccess('/api/assistant/query', 'Query processed: ' . substr($query, 0, 50) . '...');
    Response::success([
        'query' => $query,
        'response' => $response,
        'timestamp' => date('Y-m-d H:i:s')
    ]);
} catch (Exception $e) {
    Logger::logError('/api/assistant/query', $e->getMessage(), 500);
    Response::error('Failed to process query: ' . $e->getMessage(), 500);
}

function generateResponse($query) {
    $apiKey = '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';
    $model = 'google/gemma-4-26b-a4b-it:free';
    $url = 'https://openrouter.ai/api/v1/chat/completions';

    $systemPrompt = "You are WeedX Assistant, an AI helper exclusively for the WeedX precision farming application. " .
        "You ONLY answer questions related to: weed detection and treatment, robot status and battery, " .
        "crop health, soil conditions, weather for farming, field reports, and general farming advice within the app. " .
        "If the user asks anything unrelated to the WeedX app or farming, politely refuse and remind them " .
        "that you can only assist with WeedX app topics. Do not answer general knowledge, coding, politics, " .
        "entertainment, or any other off-topic questions. Be concise.";

    $payload = json_encode([
        'model' => $model,
        'messages' => [
            ['role' => 'system', 'content' => $systemPrompt],
            ['role' => 'user', 'content' => $query]
        ],
        'reasoning' => ['enabled' => true]
    ]);

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'Authorization: Bearer ' . $apiKey
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);

    $result = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    if ($result === false || $httpCode !== 200) {
        return "Sorry, I'm having trouble connecting to the AI service right now. Please try again later.";
    }

    $decoded = json_decode($result, true);
    $content = $decoded['choices'][0]['message']['content'] ?? null;

    if (!$content) {
        return "Sorry, I received an unexpected response. Please try again.";
    }

    return $content;
}
