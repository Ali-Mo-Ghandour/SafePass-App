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
    
    // Check username length
    if (strlen($username) < 3 || strlen($username) > 50) {
        throw new Exception('Username must be between 3 and 50 characters');
    }
    
    // Check password strength
    if (strlen($password) < 8 || 
        !preg_match('/[A-Z]/', $password) || 
        !preg_match('/[0-9]/', $password) || 
        !preg_match('/[^A-Za-z0-9]/', $password)) {
        throw new Exception('Password must be at least 8 characters and include uppercase, number, and special character');
    }
    
    // Check if username already exists
    $stmt = $conn->prepare('SELECT COUNT(*) FROM users WHERE username = ?');
    $stmt->execute([$username]);
    if ($stmt->fetchColumn() > 0) {
        throw new Exception('Username already exists');
    }
    
    // Generate a random salt and hash the password
    $salt = bin2hex(random_bytes(16));
    $hashed_password = password_hash($password . $salt, PASSWORD_BCRYPT);
    
    // Insert new user
    $stmt = $conn->prepare('INSERT INTO users (username, password_hash, salt, created_at) VALUES (?, ?, ?, NOW())');
    $stmt->execute([$username, $hashed_password, $salt]);
    
    $response['success'] = true;
    $response['message'] = 'Account created successfully!';
    
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