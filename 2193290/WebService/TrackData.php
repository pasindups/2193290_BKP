<?php

$vehicle_id = $_GET['vehicle_id'];
$maxrpm = $_GET['maxrpm'];
$maxspeed = $_GET['maxspeed'];
$maxfuel = $_GET['maxfuel'];
$carstatus = $_GET['carstatus'];
$time = date("Y-m-d H:i:s");


//Datasaving to database

$mysqli = new mysqli("localhost","root","","2193290");

if ($mysqli -> connect_errno) {
  echo "Failed to connect to MySQL: " . $mysqli -> connect_error;
  exit();
}

// Turn autocommit off
$mysqli -> autocommit(FALSE);

// Insert some values
$mysqli -> query("INSERT INTO trackdata('$vehicle_id','$maxrpm','$maxspeed','$maxfuel','$carstatus')");

// Commit transaction
if (!$mysqli -> commit()) {
  echo "Commit transaction failed";
  exit();
}

$mysqli -> close();