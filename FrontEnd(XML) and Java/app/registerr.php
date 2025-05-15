<?php
require_once 'db.php';  // Include the DB connection file

header('Content-Type: application/json');

// Check if the request is POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

// Check if required fields are provided
if (!isset($_POST['username']) || !isset($_POST['password'])) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$username = trim($_POST['username']);
$password = trim($_POST['password']);

// Encrypt the password
$hashedPassword = password_hash($password, PASSWORD_DEFAULT);

// Prepare SQL query to insert a new user
$query = "INSERT INTO users (username, password) VALUES (?, ?)";
$stmt = $con->prepare($query);
$stmt->bind_param("ss", $username, $hashedPassword);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Account created successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to create account']);
}

$stmt->close();
$con->close();
?>
