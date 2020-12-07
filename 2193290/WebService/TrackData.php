<?php

$vehicle_id = $_GET['vehicle_id'];
$maxrpm = $_GET['maxrpm'];
$maxspeed = $_GET['maxspeed'];
$maxfuel = $_GET['maxfuel'];
$carstatus = $_GET['carstatus'];
$time = date("Y-m-d H:i:s");


include './DatabaseConnection.php';

$mysqli -> query("INSERT INTO trackdata('$vehicle_id','$maxrpm','$maxspeed','$maxfuel','$carstatus')");

// Commit transaction
if (!$mysqli -> commit()) {
  echo "Commit transaction failed";
  exit();
}

$mysqli -> close();