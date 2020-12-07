<?php

$brand = $_POST['brand'];
$model = $_POST['model'];
$mileage = $_POST['mileage'];
$fuel_type = $_POST['fuel_type'];
$number_plate = $_POST['number_plate'];
$user_id = $_POST['user_id'];


include './DatabaseConnection.php';
$mysqli->query("INSERT INTO 'vehicle' ('vehicle_id', 'brand', 'model', 'mileage', 'fuel_type', 'number_plate', 'status', 'user_id') VALUES (NULL, '$brand', '$model', '$mileage', '$fuel_type', '$number_plate', '1', '$user_id')");

echo "New record has id: " . $mysqli->insert_id;

$mysqli->close();
//after saving vehicle data, return back to the vehicle page
header("Location:../system/vehicle.php");


