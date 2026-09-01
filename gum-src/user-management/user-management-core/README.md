# user-management-core

<!-- TOC -->
* [user-management-core](#user-management-core)
  * [Introduction](#introduction)
  * [Services](#services)
  * [Configurations](#configurations)
  * [DAO](#dao)
<!-- TOC -->

## Introduction

In this module, you will find the implementations of the APIs described [here](../user-management-api/README.md), and the
DAOs implementations.

## Services

You will find the implementations of the services *UserEditService*, *UserLoginService*, *UserLookupService*, 
*UserRegistrationService* described in [User-management-api](../user-management-api/README.md).

You will also find the *HashService*, an interface that provides a method to **encode** a String and a method to **verify** if the hash provided
is the same the raw value hashed. It has multiple implementations:
  * *MD5HashService* uses the MD5 algorithm to hash raw values
    >:warning: **Warning:** MD5 algorithm is not considered secure and is here only for legacy purpose
  * *CustomSHA1HashService* uses a custom algorithm based on SHA1 to hash raw values
    >:warning: **Warning:** SHA1 algorithm is not considered secure and is not used in a sensitive context

## Configurations

You will find here interfaces used for retrieve configuration settings.

* *ApplicationConfig* provides configuration linked to the applications
* *DatabaseConfig* provides configurations linked to the database

## DAO

You will find here all the DAOs used and their implementation.

* *UserEditDAO* is used to update users, for now only password and last login timestamp are updatable
* *UserLogin* is used to retrieve the password of a user
* *UserLookupDAO* is used for searching users
* *UserRegistration* is used to register user, for now it only retrieve the activation code of an unactivated vendor admin
user

The *UserSQLFragment* class contains full or partial SQL request constants.
