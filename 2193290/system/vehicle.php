<?php
include './header.php';
?>
<div class="main-panel">
    <div class="content">
        <div class="container-fluid">
            <div class="row">
                <div class="col-md-8">
                    <div class="card">
                        <div class="card-header" style="background-color: green">
                            <h4 class="card-title" style="color: white">Edit Vehicle</h4>
                            <p class="card-category">Complete your Vehicle details</p>
                        </div>
                        <div class="card-body">
                            <form action="../WebService/VehicleService.php" method="post">
                                <div class="row">          
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="bmd-label-floating">Vehicle Rego</label>
                                            <input type="text" name="number_plate" class="form-control">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="bmd-label-floating">Vehicle Brand</label>
                                            <input type="text" name="brand" class="form-control">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="bmd-label-floating">Model</label>
                                            <input type="text" name="model"  class="form-control">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="bmd-label-floating">ODO</label>
                                            <input type="text"  name="mileage" class="form-control">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-group">
                                            <label class="bmd-label-floating">Fuel Type</label>
                                            <select name="fuel_type" class="form-control">
                                                <option>Gasoline</option>
                                                <option>Diesel</option>
                                            </select>                                            
                                        </div>
                                    </div>
                                </div>

                                <?php                                
                                $user_id = $_SESSION['user_id'];
                                echo '<input type="hidden" name="user_id" value="' . $user_id . '"/>';
                                ?>

                                <button type="submit" class="btn btn-primary pull-right" style="background-color: green">Update Vehicle</button>
                                <div class="clearfix"></div>
                            </form>
                        </div>
                    </div>
                </div>                
            </div>
        </div>
    </div>
</div>

