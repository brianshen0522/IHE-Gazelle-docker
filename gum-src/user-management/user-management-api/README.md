# User-management-api

<!-- TOC -->
* [User-management-api](#user-management-api)
  * [Introduction](#introduction)
  * [Models](#models)
    * [Groups](#groups)
  * [Services](#services)
<!-- TOC -->

## Introduction

In this module, you will find the APIs and Models of Gazelle-User-Management.

## Models

* The user model is used as a simplified representation of a user stored in our external database. We use it in the
  *GazelleUserModelAdapter*. There is also a delegated representation for user to know to which identity provider is
  bound to.
* The credentials model is used to store all information relative to a user password.
* The model UserPreference is here to allow user to change its preferences.
* The role and group model are a very simplified representation of what can exist in Test Management.

### Groups

Since version 4.0.0, roles no longer exist in GUM as they are replaced by groups. Depending on the context, groups can
have multiple meanings:

- User that belongs to the Organization A is the same as saying it belongs to a group called "Organization A" (or at
  least it will in the 5.0.0 version)
- User that as the role "administrator" is the same as saying it belongs to a group called "administrator"

Also groups can be in another groups. This will allow us fine-tuning of authorization.

For example, we have organization members and organization administrator. Let's say that organization member can read
resources of the organization and organization administrator can read and update them. If we give a permission to read
to organization member and permissions to read and edit to an organization administrator, we see that the read
permission
is duplicated.

Now let's say that organization administrator is a group inside the organization member group. It means that a user in
the group of organization administrator is also in the group organization member. So now if we give the permission to
read resources to the group organization member, the group organization administrator will also get it.

In GUM, we separated groups in two category:
- Static ones
- Dynamic ones

We consider that group is "static" if its name is a static one and is the same for everyone. For example all roles
(except organization administrator) are static groups because their name never change. The name of the Gazelle administrator 
group is `role:gazelle_admin`. 

As for the other category, we consider them dynamic if the name can be different for two given users. For example, we want
to put different users in the group "organization administrator", but these users are not from the same organization. So
the name of the group will start with the prefix `org-adm:` followed by the id of the organization. For example, a user in
the group `org-adm:company-id` means that he is a organization administrator for the organization corresponding to 
the id `company-id`.

You will find all static groups and the prefix of the dynamic ones in the class
[GazelleDefaultGroup](./src/main/java/net/ihe/gazelle/user/management/api/domain/user/GazelleDefaultGroup.java).



## Services

* **UserEditService** is used when Keycloak needs to edit a user, for now only the user password and its last login
  timestamp are editable.
* **UserLoginService** is used when Keycloak needs to validate the credentials of a user logging in.
* **UserLookupService** is used when Keycloak needs to search one or more users.
* **UserRegistrationService** is used when we need to retrieve the activation code for a user with an inactivated account.
* **OrganizationLookupService** is used when need to search for Organizations.
* **OrganizationManagementService** is used when we need to create/update organizations. It can also be used to leave or
  join an Organization for a given user
* **DelegatedOrganizationService** is used when we need to work with delegated Organizations. It is used to create new
  delegated Organizations or to transform existing Organizations to delegated ones.
* **GroupService** is used when we need to do CRUD actions on groups. It can also be used to join or leave a group for a
  given user