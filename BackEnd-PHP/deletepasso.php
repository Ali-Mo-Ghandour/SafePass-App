<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

header('Content-Type: application/json');

// Database configuration
$db_host = 'localhost';
$db_name = 'password_manager';
$db_user = 'root';  // Change this to your MySQL username
$db_pass = '';      // Change this to your MySQL password

// Response array
$response = array();

try {
    // Log all POST data for debugging
    error_log("POST data received: " . print_r($_POST, true));
    
    // Connect to database
    $conn = new PDO("mysql:host=$db_host;dbname=$db_name", $db_user, $db_pass);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get POST data
    $user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
    $password_id = isset($_POST['id']) ? trim($_POST['id']) : '';
    
    // Log received data
    error_log("Received user_id: " . $user_id);
    error_log("Received password_id: " . $password_id);
    
    // Validate input
    if (empty($user_id) || empty($password_id)) {
        throw new Exception('User ID and password ID are required. Received user_id: ' . $user_id . ', password_id: ' . $password_id);
    }
    
    // Verify password belongs to user
    $stmt = $conn->prepare('SELECT id FROM passwords WHERE id = ? AND user_id = ?');
    $stmt->execute([$password_id, $user_id]);
    if (!$stmt->fetch()) {
        throw new Exception('Password not found or unauthorized');
    }
    
    // Delete the password
    $stmt = $conn->prepare('DELETE FROM passwords WHERE id = ? AND user_id = ?');
    $stmt->execute([$password_id, $user_id]);
    
    if ($stmt->rowCount() === 0) {
        throw new Exception('Failed to delete password');
    }
    
    $response['success'] = true;
    $response['message'] = 'Password deleted successfully';
    
} catch (PDOException $e) {
    // Log the specific database error
    error_log("Database Error: " . $e->getMessage());
    $response['success'] = false;
    $response['message'] = 'Database error: ' . $e->getMessage();
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