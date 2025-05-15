<?php
header('Content-Type: application/json');

// Database configuration
$db_host = 'localhost';
$db_name = 'password_manager';
$db_user = 'root';  // Change this to your MySQL username
$db_pass = '';      // Change this to your MySQL password

// Response array
$response = array();

try {
    // Connect to database
    $conn = new PDO("mysql:host=$db_host;dbname=$db_name", $db_user, $db_pass);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get POST data
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    $user_key = isset($_POST['user_key']) ? trim($_POST['user_key']) : '';
    
    // Validate input
    if (empty($user_id) || empty($user_key)) {
        throw new Exception('User ID and key are required');
    }
    
    // Get user's salt
    $stmt = $conn->prepare('SELECT salt FROM users WHERE id = ?');
    $stmt->execute([$user_id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        throw new Exception('Invalid user');
    }
    
    // Get all passwords for the user
    $stmt = $conn->prepare('SELECT id, account, username, password, iv, notes, created_at FROM passwords WHERE user_id = ? ORDER BY created_at DESC');
    $stmt->execute([$user_id]);
    $passwords = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Decrypt each password
    $encryption_key = hash('sha256', $user_key . $user['salt'], true);
    $decrypted_passwords = array();
    
    foreach ($passwords as $password) {
        $iv = base64_decode($password['iv']);
        $decrypted_password = openssl_decrypt(
            $password['password'],
            'aes-256-cbc',
            $encryption_key,
            0,
            $iv
        );
        
        if ($decrypted_password === false) {
            throw new Exception('Failed to decrypt password');
        }
        
        $decrypted_passwords[] = array(
            'id' => $password['id'],
            'account' => $password['account'],
            'username' => $password['username'],
            'password' => $decrypted_password,
            'notes' => $password['notes'],
            'createdAt' => $password['created_at']
        );
    }
    
    $response['success'] = true;
    $response['message'] = 'Passwords retrieved successfully';
    $response['data'] = $decrypted_passwords;
    
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