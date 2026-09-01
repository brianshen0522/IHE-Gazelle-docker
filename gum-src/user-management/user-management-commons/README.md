# user-management-commons

<!-- TOC -->
* [user-management-commons](#user-management-commons)
  * [Image resizing](#image-resizing)
  * [Hash Services](#hash-services)
    * [Password hash](#password-hash)
    * [Custom hash](#custom-hash)
  * [DAOs](#daos)
  * [Entities](#entities-)
<!-- TOC -->

## Image resizing

With the migration of user preferences in Gazelle user management, we needed to store the user profile picture. We also 
needed to be able to resize them and generate thumbnails.

We create a service called ImageTransformationService that expose two methods, one to transform an image to JPEG format 
and one to generate a thumbnail of this image.

We implemented this service with the use of the imgscalr library. Here is the link to the GitHub repository 
[https://github.com/rkalla/imgscalr](https://github.com/rkalla/imgscalr).

Information on how to use it [https://www.baeldung.com/java-resize-image#imgscalr](https://www.baeldung.com/java-resize-image#imgscalr).


## Hash Services

### Password hash

We have multiple two hash password services implementations: 

- **MD5HashService**: hash the password using the MD5 algorithm.
>:warning: **Warning**: The MD5 algorithm is not recognized as secured, it should not be used for sensitive information. 
> It is only here for legacy purposes.
- **PBKDF2HashService**: hash the password using the PBKDF2 algorithm, it is secured and can be used for sensitive information.


### Custom hash
There also another hash service implementation that is not used for password hash:

- **CustomSHA1HashService**: This is a custom hash algorithm based on SHA1 that will always return a hash of 20 characters.
>:warning: **Warning**: As SHA1 is not recognized as secure, this implementation should not be used for sensitive information.


## DAOs

In this module you will find DAOs interfaces (in /application) and their implementation (in /interlay). These DAOs are 
the classes that will make actions (read, write, delete, ...) on the database. There are named after the service that uses
them.

## Entities 

The entities here are the representation, of their counterparts of the models from the user-management-api module, that 
will be stored in the database. 