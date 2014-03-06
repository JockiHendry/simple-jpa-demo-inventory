CREATE DATABASE  IF NOT EXISTS `inventory` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `inventory`;
SET FOREIGN_KEY_CHECKS = 0;
-- MySQL dump 10.13  Distrib 5.6.11, for Win32 (x86)
--
-- Host: 127.0.0.1    Database: inventory
-- ------------------------------------------------------
-- Server version	5.6.11-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `faktur`
--

/*!40000 ALTER TABLE `faktur` DISABLE KEYS */;
/*!40000 ALTER TABLE `faktur` ENABLE KEYS */;

--
-- Dumping data for table `gudang`
--

/*!40000 ALTER TABLE `gudang` DISABLE KEYS */;
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-7,'2014-02-22 00:00:00','N',NULL,NULL,'Warehouse F','\0');
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-6,'2014-02-22 00:00:00','N',NULL,NULL,'Warehouse E','\0');
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-5,'2014-02-22 00:00:00','N',NULL,NULL,'Warehouse D','\0');
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-4,'2014-02-22 00:00:00','N',NULL,NULL,'Warehouse C','\0');
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-3,'2014-02-22 00:00:00','N',NULL,NULL,'Warehouse B','\0');
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-2,'2014-02-22 00:00:00','N',NULL,NULL,'Warehouse A','\0');
INSERT INTO `gudang` (`id`, `createdDate`, `deleted`, `keterangan`, `modifiedDate`, `nama`, `utama`) VALUES (-1,'2014-02-22 00:00:00','N',NULL,NULL,'Gudang','');
/*!40000 ALTER TABLE `gudang` ENABLE KEYS */;

--
-- Dumping data for table `hibernate_sequences`
--

/*!40000 ALTER TABLE `hibernate_sequences` DISABLE KEYS */;
/*!40000 ALTER TABLE `hibernate_sequences` ENABLE KEYS */;

--
-- Dumping data for table `itemfaktur`
--

/*!40000 ALTER TABLE `itemfaktur` DISABLE KEYS */;
/*!40000 ALTER TABLE `itemfaktur` ENABLE KEYS */;

--
-- Dumping data for table `periodeitemstok`
--

/*!40000 ALTER TABLE `periodeitemstok` DISABLE KEYS */;
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-18,'\0','2014-02-22 00:00:00','N',10,NULL,'2010-01-01','2010-01-31',-14,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-17,'\0','2014-02-22 00:00:00','N',4,NULL,'2014-01-01','2014-01-31',-13,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-16,'\0','2014-02-22 00:00:00','N',9,NULL,'2014-02-01','2014-02-28',-12,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-15,'\0','2014-02-22 00:00:00','N',8,NULL,'2014-02-01','2014-02-28',-11,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-14,'\0','2014-02-22 00:00:00','N',7,NULL,'2014-02-01','2014-02-28',-10,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-13,'\0','2014-02-22 00:00:00','N',1,NULL,'2014-02-01','2014-02-28',-9,1);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-12,'\0','2014-02-22 00:00:00','N',5,NULL,'2014-01-01','2014-01-31',-9,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-11,'\0','2014-02-22 00:00:00','N',2,NULL,'2014-02-01','2014-02-28',-8,1);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-10,'\0','2014-02-22 00:00:00','N',2,NULL,'2014-01-01','2014-01-31',-8,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-9,'\0','2014-02-22 00:00:00','N',3,NULL,'2014-01-01','2014-01-31',-7,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-8,'\0','2014-02-22 00:00:00','N',2,NULL,'2014-01-01','2014-01-31',-6,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-7,'\0','2014-02-22 00:00:00','N',3,NULL,'2014-01-01','2014-01-31',-5,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-6,'\0','2014-02-22 00:00:00','N',5,NULL,'2014-01-01','2014-01-31',-4,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-5,'\0','2014-02-22 00:00:00','N',10,NULL,'2014-01-01','2014-01-31',-3,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-4,'\0','2014-02-22 00:00:00','N',2,NULL,'2014-02-01','2014-02-28',-2,1);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-3,'\0','2014-02-22 00:00:00','N',2,NULL,'2014-01-01','2014-01-31',-2,0);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-2,'\0','2014-02-22 00:00:00','N',2,NULL,'2014-01-01','2014-01-31',-1,1);
INSERT INTO `periodeitemstok` (`id`, `arsip`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `tanggalMulai`, `tanggalSelesai`, `stokProduk_id`, `daftarPeriodeItemStok_ORDER`) VALUES (-1,'\0','2014-02-22 00:00:00','N',3,NULL,'2013-12-01','2013-12-31',-1,0);
/*!40000 ALTER TABLE `periodeitemstok` ENABLE KEYS */;

--
-- Dumping data for table `periodeitemstok_listitemstok`
--

/*!40000 ALTER TABLE `periodeitemstok_listitemstok` DISABLE KEYS */;
/*!40000 ALTER TABLE `periodeitemstok_listitemstok` ENABLE KEYS */;

--
-- Dumping data for table `produk`
--

/*!40000 ALTER TABLE `produk` DISABLE KEYS */;
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-11,'2014-02-22 00:00:00','N',100.00,10,NULL,'Produk Z');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-10,'2014-02-22 00:00:00','N',1400.00,0,NULL,'Produk J');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-9,'2014-02-22 00:00:00','N',300.00,0,NULL,'Produk I');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-8,'2014-02-22 00:00:00','N',400.00,0,NULL,'Produk H');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-7,'2014-02-22 00:00:00','N',100.00,0,NULL,'Produk G');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-6,'2014-02-22 00:00:00','N',800.00,0,NULL,'Produk F');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-5,'2014-02-22 00:00:00','N',500.00,0,NULL,'Produk E');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-4,'2014-02-22 00:00:00','N',13000.00,0,NULL,'Produk D');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-3,'2014-02-22 00:00:00','N',900.00,21,NULL,'Produk C');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-2,'2014-02-22 00:00:00','N',1500.00,17,NULL,'Produk B');
INSERT INTO `produk` (`id`, `createdDate`, `deleted`, `harga`, `jumlah`, `modifiedDate`, `nama`) VALUES (-1,'2014-02-22 00:00:00','N',1000.00,32,NULL,'Produk A');
/*!40000 ALTER TABLE `produk` ENABLE KEYS */;

--
-- Dumping data for table `stokproduk`
--

/*!40000 ALTER TABLE `stokproduk` DISABLE KEYS */;
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-14,'2014-02-22 00:00:00','N',10,NULL,-1,-11);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-13,'2014-02-22 00:00:00','N',4,NULL,-5,-3);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-12,'2014-02-22 00:00:00','N',9,NULL,-4,-3);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-11,'2014-02-22 00:00:00','N',8,NULL,-1,-3);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-10,'2014-02-22 00:00:00','N',7,NULL,-3,-2);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-9,'2014-02-22 00:00:00','N',6,NULL,-2,-2);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-8,'2014-02-22 00:00:00','N',4,NULL,-1,-2);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-7,'2014-02-22 00:00:00','N',3,NULL,-7,-1);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-6,'2014-02-22 00:00:00','N',2,NULL,-6,-1);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-5,'2014-02-22 00:00:00','N',3,NULL,-5,-1);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-4,'2014-02-22 00:00:00','N',5,NULL,-4,-1);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-3,'2014-02-22 00:00:00','N',10,NULL,-3,-1);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-2,'2014-02-22 00:00:00','N',4,NULL,-2,-1);
INSERT INTO `stokproduk` (`id`, `createdDate`, `deleted`, `jumlah`, `modifiedDate`, `gudang_id`, `produk_id`) VALUES (-1,'2014-02-22 00:00:00','N',5,NULL,-1,-1);
/*!40000 ALTER TABLE `stokproduk` ENABLE KEYS */;

--
-- Dumping data for table `supplier`
--

/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-11,'Manado','2014-02-22 00:00:00','N',NULL,'Kalbe Farma','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-10,'Minahasa','2014-02-22 00:00:00','N',NULL,'Surya Esa Perkasa','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-9,'Semarang','2014-02-22 00:00:00','N',NULL,'Berlian Laju Tanker','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-8,'Lampung','2014-02-22 00:00:00','N',NULL,'Asia Natural Resources','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-7,'Papua','2014-02-22 00:00:00','N',NULL,'Asiaplast Industries','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-6,'Pontianak','2014-02-22 00:00:00','N',NULL,'Alfa Retalindo','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-5,'Palembang','2014-02-22 00:00:00','N',NULL,'Majapahit Securities','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-4,'Bali','2014-02-22 00:00:00','N',NULL,'Polychem Indonesia','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-3,'Surabaya','2014-02-22 00:00:00','N',NULL,'Ace Hardware Indonesia','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-2,'Bandung','2014-02-22 00:00:00','N',NULL,'ABM Ivestama','');
INSERT INTO `supplier` (`id`, `alamat`, `createdDate`, `deleted`, `modifiedDate`, `nama`, `nomorTelepon`) VALUES (-1,'Jakarta','2014-02-22 00:00:00','N',NULL,'Astra Agro Lestari','021-4616555');
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

SET FOREIGN_KEY_CHECKS = 1;
-- Dump completed
