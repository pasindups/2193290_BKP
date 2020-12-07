<?php

$vehicle_id = $_GET['vehicle_id'];
$err_msg = $_GET['errormsg'];
$rectime = date("Y-m-d H:i:s");

include './DatabaseConnection.php';
$mysqli->query("insert into errors values ('0','$vehicle_id','$err_msg','$rectime','1')");

$status = array("status"=>$mysqli->insert_id);

//Change the header type to JSON
header('Content-type: application/json');
echo json_encode($status);

