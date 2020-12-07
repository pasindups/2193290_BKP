<?php

$username = $_POST['uname'];
$password = md5($_POST['psw']);

include './DatabaseConnection.php';

$sql = "SELECT user_id FROM users where username='$username' AND password='$password'";
$result = $mysqli->query($sql);
if ($row = $result->fetch_assoc()) {
    session_start();
    $_SESSION['user_id'] = $row['user_id'];
    header("Location:../system/dashboard.php");
} else {
    header("Location:../system/login.php?msg=You have entered an invalid username or password");
}

