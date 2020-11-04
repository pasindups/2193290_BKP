<?php

$vehicle_id = $_GET['vehicle_id'];
$lat = $_GET['lat'];
$lon = $_GET['lon'];
$time = date("Y-m-d H:i:s");

$isDataSave = TRUE;

//Datasaving to database

$mysqli = new mysqli("localhost", "root", "", "2193290");

if ($mysqli->connect_errno) {
    echo "Failed to connect to MySQL: " . $mysqli->connect_error;
    $isDataSave = FALSE;
}

// Turn autocommit off
$mysqli->autocommit(FALSE);

// Insert some values
$mysqli->query("INSERT INTO locations('$vehicle_id','$lat','$lon','$time')");

// Commit transaction
if (!$mysqli->commit()) {
    echo "Commit transaction failed";
    $isDataSave = FALSE;
}

$mysqli->close();

//Change the header type to JSON
header('Content-type: application/json');

if ($isDataSave) {
    echo json_encode(array('msg' => 'Error while saving data'));
} else {
     echo json_encode(array('msg' => 'Data saved successfully'));
}

