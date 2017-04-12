-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 12, 2017 at 04:24 AM
-- Server version: 5.7.17-0ubuntu0.16.04.2
-- PHP Version: 7.0.15-0ubuntu0.16.04.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `JournalRepo`
--

-- --------------------------------------------------------

--
-- Table structure for table `Agency`
--

CREATE TABLE `Agency` (
  `idAgency` int(11) NOT NULL,
  `engName` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `korName` varchar(45) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Article`
--

CREATE TABLE `Article` (
  `idArticle` bigint(20) NOT NULL,
  `idAgency` int(11) NOT NULL,
  `idWeb` varchar(24) COLLATE utf8_unicode_ci NOT NULL,
  `title` varchar(400) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ArticleBody`
--

CREATE TABLE `ArticleBody` (
  `idArticle` bigint(20) NOT NULL,
  `idAgency` int(11) NOT NULL,
  `idWeb` varchar(24) COLLATE utf8_unicode_ci NOT NULL,
  `title` varchar(400) COLLATE utf8_unicode_ci DEFAULT NULL,
  `body` text COLLATE utf8_unicode_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ArticleReporter`
--

CREATE TABLE `ArticleReporter` (
  `idArticle` bigint(20) NOT NULL,
  `idReporter` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `HTTPProxy`
--

CREATE TABLE `HTTPProxy` (
  `idProxy` int(11) NOT NULL,
  `address` varchar(16) COLLATE utf8_unicode_ci DEFAULT NULL,
  `alive` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Dumping data for table `HTTPProxy`
--

INSERT INTO `HTTPProxy` (`idProxy`, `address`, `alive`) VALUES
(1, '97.77.104.22', 1);

-- --------------------------------------------------------

--
-- Table structure for table `Reporter`
--

CREATE TABLE `Reporter` (
  `idReporter` int(11) NOT NULL,
  `idAgency` int(11) NOT NULL,
  `name` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  `emailAddress` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Agency`
--
ALTER TABLE `Agency`
  ADD PRIMARY KEY (`idAgency`);

--
-- Indexes for table `Article`
--
ALTER TABLE `Article`
  ADD PRIMARY KEY (`idArticle`),
  ADD UNIQUE KEY `idWeb` (`idWeb`),
  ADD KEY `fk_Article_1_idx` (`idAgency`);

--
-- Indexes for table `ArticleBody`
--
ALTER TABLE `ArticleBody`
  ADD PRIMARY KEY (`idArticle`),
  ADD UNIQUE KEY `idWeb` (`idWeb`),
  ADD KEY `fk_ArticleBody_1_idx` (`idAgency`);

--
-- Indexes for table `ArticleReporter`
--
ALTER TABLE `ArticleReporter`
  ADD KEY `fk_ArticleReporter_1_idx` (`idReporter`),
  ADD KEY `fk_ArticleReporter_2_idx` (`idArticle`),
  ADD KEY `idArticle` (`idArticle`,`idReporter`),
  ADD KEY `idReporter` (`idReporter`,`idArticle`);

--
-- Indexes for table `HTTPProxy`
--
ALTER TABLE `HTTPProxy`
  ADD PRIMARY KEY (`idProxy`);

--
-- Indexes for table `Reporter`
--
ALTER TABLE `Reporter`
  ADD PRIMARY KEY (`idReporter`),
  ADD KEY `fk_Reporter_1_idx` (`idAgency`),
  ADD KEY `name` (`name`,`emailAddress`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `Agency`
--
ALTER TABLE `Agency`
  MODIFY `idAgency` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=140;
--
-- AUTO_INCREMENT for table `Article`
--
ALTER TABLE `Article`
  MODIFY `idArticle` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;
--
-- AUTO_INCREMENT for table `ArticleBody`
--
ALTER TABLE `ArticleBody`
  MODIFY `idArticle` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=45;
--
-- AUTO_INCREMENT for table `HTTPProxy`
--
ALTER TABLE `HTTPProxy`
  MODIFY `idProxy` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
--
-- AUTO_INCREMENT for table `Reporter`
--
ALTER TABLE `Reporter`
  MODIFY `idReporter` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3017;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `Article`
--
ALTER TABLE `Article`
  ADD CONSTRAINT `fk_Article_1` FOREIGN KEY (`idAgency`) REFERENCES `Agency` (`idAgency`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `ArticleBody`
--
ALTER TABLE `ArticleBody`
  ADD CONSTRAINT `fk_ArticleBody_1` FOREIGN KEY (`idAgency`) REFERENCES `Agency` (`idAgency`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `ArticleReporter`
--
ALTER TABLE `ArticleReporter`
  ADD CONSTRAINT `fk_ArticleReporter_1` FOREIGN KEY (`idReporter`) REFERENCES `Reporter` (`idReporter`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_ArticleReporter_2` FOREIGN KEY (`idArticle`) REFERENCES `Article` (`idArticle`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `Reporter`
--
ALTER TABLE `Reporter`
  ADD CONSTRAINT `fk_Reporter_Agency` FOREIGN KEY (`idAgency`) REFERENCES `Agency` (`idAgency`) ON DELETE NO ACTION ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
