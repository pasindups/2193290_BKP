-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 07, 2020 at 03:23 AM
-- Server version: 10.4.11-MariaDB
-- PHP Version: 7.2.29

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `2193290`
--
CREATE DATABASE IF NOT EXISTS `2193290` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `2193290`;

-- --------------------------------------------------------

--
-- Table structure for table `errors`
--

CREATE TABLE `errors` (
  `error_id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `error_data` varchar(100) NOT NULL,
  `record_time` datetime NOT NULL DEFAULT current_timestamp(),
  `status` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `errors`
--

INSERT INTO `errors` (`error_id`, `vehicle_id`, `error_data`, `record_time`, `status`) VALUES
(1, 0, '', '2020-11-04 11:48:38', 0),
(2, 0, '', '2020-11-04 11:48:57', 0),
(3, 0, '', '2020-11-04 11:49:01', 0),
(4, 0, '', '2020-12-05 10:17:28', 0),
(5, 0, '', '2020-12-04 22:20:53', 1),
(6, 0, '', '2020-12-04 22:21:14', 1),
(7, 1, 'Test', '2020-12-04 22:21:43', 1),
(8, 1, 'Test', '2020-12-04 22:21:52', 1),
(9, 1, 'Test', '2020-12-04 22:23:01', 1),
(10, 1, 'Test', '2020-12-04 22:27:22', 1);

-- --------------------------------------------------------

--
-- Table structure for table `locations`
--

CREATE TABLE `locations` (
  `id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `lat` double NOT NULL,
  `lon` double NOT NULL,
  `record_time` datetime NOT NULL DEFAULT current_timestamp(),
  `status` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `trackdata`
--

CREATE TABLE `trackdata` (
  `track_id` int(11) NOT NULL,
  `vehicle_id` int(11) NOT NULL,
  `maxrpm` double NOT NULL,
  `maxspeed` double NOT NULL,
  `maxfuel` double NOT NULL,
  `carstatus` int(11) NOT NULL,
  `recordtime` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(200) NOT NULL,
  `full_name` varchar(200) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(50) NOT NULL,
  `status` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `full_name`, `email`, `phone`, `status`) VALUES
(1, 'pasi', '202cb962ac59075b964b07152d234b70', 'Pasindu', 'pasindups@gmail.com', '0212972914', 1);

-- --------------------------------------------------------

--
-- Table structure for table `vehicle`
--

CREATE TABLE `vehicle` (
  `vehicle_id` int(11) NOT NULL,
  `brand` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  `mileage` double NOT NULL,
  `fuel_type` varchar(10) NOT NULL,
  `number_plate` varchar(10) NOT NULL,
  `status` int(11) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `vehicle`
--

INSERT INTO `vehicle` (`vehicle_id`, `brand`, `model`, `mileage`, `fuel_type`, `number_plate`, `status`, `user_id`) VALUES
(4, 'Nissan', 'Note', 69036, 'Gasoline', 'MKH253', 1, 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `errors`
--
ALTER TABLE `errors`
  ADD PRIMARY KEY (`error_id`);

--
-- Indexes for table `locations`
--
ALTER TABLE `locations`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `trackdata`
--
ALTER TABLE `trackdata`
  ADD PRIMARY KEY (`track_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `vehicle`
--
ALTER TABLE `vehicle`
  ADD PRIMARY KEY (`vehicle_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `errors`
--
ALTER TABLE `errors`
  MODIFY `error_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `locations`
--
ALTER TABLE `locations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `trackdata`
--
ALTER TABLE `trackdata`
  MODIFY `track_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `vehicle`
--
ALTER TABLE `vehicle`
  MODIFY `vehicle_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
