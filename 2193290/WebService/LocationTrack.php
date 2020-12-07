<?php

$vehicle_id = $_GET['vehicle_id'];
$lat = $_GET['lat'];
$lon = $_GET['lon'];
$time = date("Y-m-d H:i:s");

$isDataSave = TRUE;

include './DatabaseConnection.php';

// Insert values
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

