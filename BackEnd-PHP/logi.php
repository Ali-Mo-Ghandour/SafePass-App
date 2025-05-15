<?php
header('Content-Type: application/json');

// Database configuration
$db_host = 'localhost';
$db_name = 'password_manager';
$db_user = 'root';
$db_pass = '';

// Response array
$response = array();

try {
    // Connect to database
    $conn = new PDO("mysql:host=$db_host;dbname=$db_name", $db_user, $db_pass);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get POST data
    $username = isset($_POST['username']) ? trim($_POST['username']) : '';
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';
    
    // Validate input
    if (empty($username) || empty($password)) {
        throw new Exception('Username and password are required');
    }
    
    // Get user from database
    $stmt = $conn->prepare('SELECT id, username, password_hash, salt FROM users WHERE username = ?');
    $stmt->execute([$username]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        // User not found, but don't reveal this specific information
        throw new Exception('Invalid username or password');
    }
    
    // Verify password
    if (!password_verify($password . $user['salt'], $user['password_hash'])) {
        // Password incorrect, but don't reveal this specific information
        throw new Exception('Invalid username or password');
    }
    
    // Login successful
    $response['success'] = true;
    $response['message'] = 'Login successful';
    $response['user_id'] = $user['id'];
    
    // Update last login timestamp (optional)
    $stmt = $conn->prepare('UPDATE users SET updated_at = NOW() WHERE id = ?');
    $stmt->execute([$user['id']]);
    
} catch (Exception $e) {
    $response['success'] = false;
    $response['message'] = $e->getMessage();
} finally {
    // Close connection
    $conn = null;
}

// Send response
echo json_encode($response);
?>