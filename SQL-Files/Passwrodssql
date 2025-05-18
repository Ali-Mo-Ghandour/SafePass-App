-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 15, 2025 at 11:08 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `password_manager`
--

-- --------------------------------------------------------

--
-- Table structure for table `passwords`
--

CREATE TABLE `passwords` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `account` varchar(100) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` text NOT NULL,
  `iv` varchar(64) NOT NULL,
  `notes` text DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `passwords`
--

INSERT INTO `passwords` (`id`, `user_id`, `account`, `username`, `password`, `iv`, `notes`, `created_at`, `updated_at`) VALUES
(4, 1, 'hi', 'hi', '+3MgWuh84Zx5AqUOJQPFog==', 'JPS+9VCDazBlnW2N+6630Q==', 'shususs', '2025-04-27 23:57:00', '2025-04-27 23:57:00'),
(5, 3, 'facebook', 'abbas123', 'f2vMgspWOZnLfdG8bPLfZg==', 'Prbsx9JGEMIvEfqSdP/HNg==', 'bla bla bla', '2025-04-28 09:29:48', '2025-04-28 09:30:10'),
(6, 3, 'tiktok', 'abbas', 'G/VJ0RrSy7SGUY7vweXvBw==', 'koDZXZjNAlSpixVAIdNS7A==', 'hahahah', '2025-04-28 09:30:32', '2025-04-28 09:30:32'),
(7, 3, 'gmail', 'leonard', 'F/Z6Cls8rHKWRB1+SEmLmg==', 'SedN0f71EKC7kKYQbinCVA==', 'ana l malek', '2025-04-28 09:37:26', '2025-04-28 09:37:26');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `passwords`
--
ALTER TABLE `passwords`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `passwords`
--
ALTER TABLE `passwords`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `passwords`
--
ALTER TABLE `passwords`
  ADD CONSTRAINT `passwords_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
