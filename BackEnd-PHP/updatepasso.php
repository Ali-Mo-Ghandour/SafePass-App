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
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    $user_key = isset($_POST['user_key']) ? trim($_POST['user_key']) : '';
    $password_id = isset($_POST['id']) ? trim($_POST['id']) : '';
    $account = isset($_POST['account']) ? trim($_POST['account']) : '';
    $username = isset($_POST['username']) ? trim($_POST['username']) : '';
    $password = isset($_POST['pass']) ? trim($_POST['pass']) : '';
    $notes = isset($_POST['notes']) ? trim($_POST['notes']) : '';
    
    // Validate input
    if (empty($user_id) || empty($user_key) || empty($password_id) || empty($account) || empty($username) || empty($password)) {
        throw new Exception('All fields are required');
    }
    
    // Verify user exists and get their salt
    $stmt = $conn->prepare('SELECT salt FROM users WHERE id = ?');
    $stmt->execute([$user_id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$user) {
        throw new Exception('Invalid user');
    }
    
    // Verify password belongs to user
    $stmt = $conn->prepare('SELECT id FROM passwords WHERE id = ? AND user_id = ?');
    $stmt->execute([$password_id, $user_id]);
    if (!$stmt->fetch()) {
        throw new Exception('Password not found or unauthorized');
    }
    
    // Encrypt the password using user's key and salt
    $encryption_key = hash('sha256', $user_key . $user['salt'], true);
    $iv = openssl_random_pseudo_bytes(openssl_cipher_iv_length('aes-256-cbc'));
    $encrypted_password = openssl_encrypt($password, 'aes-256-cbc', $encryption_key, 0, $iv);
    
    if ($encrypted_password === false) {
        throw new Exception('Encryption failed');
    }
    
    // Update the password
    $stmt = $conn->prepare('UPDATE passwords SET account = ?, username = ?, password = ?, iv = ?, notes = ?, updated_at = NOW() WHERE id = ? AND user_id = ?');
    $stmt->execute([
        $account,
        $username,
        $encrypted_password,
        base64_encode($iv),
        $notes,
        $password_id,
        $user_id
    ]);
    
    $response['success'] = true;
    $response['message'] = 'Password updated successfully';
    
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