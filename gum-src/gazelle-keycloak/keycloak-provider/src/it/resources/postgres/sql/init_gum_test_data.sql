--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET search_path = public, pg_catalog;


---
-- Insert user
---
-- user1
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('user1','user1@gazelle.com','user1 fn','user1 ln',true,'2020-10-05 14:01:10.00','INSTITution1','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials,reset_password) values ('user1', '{"password":"505c1cf582bdcac5f9acd559b142858e", "hashMethod":"MD5"}',true);
-- user2
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('user2','user2@gazelle.com','user2 fn','user2 ln',true,'2020-10-05 14:01:10.00','institution2','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('user2', '{"password":"5810471f5212570eed41d6828e13dc9100a4e22ef241375ea71215c3d813458fa8bc01af9e1291695f52f2973c756fed1b5930ab1e127627472eb6a3f4f866b4", "salt":"gFzJUv2L5xpKvy2MiNLbgw==", "iterations":"10000", "hashMethod":"PBKDF2"}',false);
-- user3
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('user3','user3@gazelle.com','user3 fn','user3 ln',true,'2020-10-05 14:01:10.00','institution3','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('user3', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);
-- user4
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('user4','user4@gazelle.com','user4 fn','user4 ln',true,'2020-10-05 14:01:10.00','institution4','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('user4', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);
-- user5
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('user5','user5@gazelle.com','user5 fn','user5 ln',true,'2020-10-05 14:01:10.00','institution4','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('user5', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);
-- user6
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('user6','user6@gazelle.com','user6 fn','user6 ln',true,'2020-10-05 14:01:10.00','institution4','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('user6', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);
-- USEr6
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('USEr6','user6b@gazelle.com','USEr6 fn','USEr6 ln',true,'2020-10-05 14:01:10.00','institution4','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('USEr6', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);
-- inactiveUser
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('inactiveUser','inactive1@gazelle.com','inactive1_firstname','inactive1_lastname',false,'2020-10-05 14:01:10.00','institution4','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials, reset_password) values ('inactiveUser', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);
-- inactiveUser2
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,activation_code,last_update_timestamp,registration_timestamp,login_counter) values
    ('inactiveUser2','inactive2@gazelle.com','inactive2_firstname','inactive2_lastname',false,'2020-10-05 14:01:10.00','institution4','code_activation','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',0);
insert into gum.credentials (user_id,credentials, reset_password) values ('inactiveUser2', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);

-- inactiveVendor
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,activation_code,last_update_timestamp,registration_timestamp,login_counter) values
    ('inactiveVendor','inactiveVendor@gazelle.com','inactiveVendor_firstname','inactiveVendor_lastname',false,'2020-10-05 14:01:10.00','INSTITution1','code_activation','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',0);
insert into gum.credentials (user_id,credentials, reset_password) values ('inactiveVendor', '{"password":"505c1cf582bdcac5f9acd559b142858e", "salt":"testSalt", "iterations":"1000", "hashMethod":"MD5"}',false);


---
-- Insert group
---
insert into gum.group (id,type,reference) values ('org-adm:INSTITution1','ORGANIZATION_ADMIN','INSTITution1');
insert into gum.group (id,type,reference) values ('org-adm:institution3','ORGANIZATION_ADMIN','institution3');
insert into gum.group (id,type,reference) values ('org-adm:institution4','ORGANIZATION_ADMIN','institution4');

---
-- Insert user group
---
-- user1 -> gazelle_admin
insert into gum.user_group (user_id,group_id) values ((select id from gum.user where id = 'user1'), 'role:gazelle_admin');
-- user2 -> monitor
insert into gum.user_group (user_id,group_id) values ((select id from gum.user where id = 'user2'), 'role:monitor');
-- user3 -> org-adm
insert into gum.user_group (user_id,group_id) values ((select id from gum.user where id = 'user3'), 'org-adm:institution3');
-- inactiveVendor -> org-adm
insert into gum.user_group (user_id,group_id) values ((select id from gum.user where id = 'inactiveVendor'), 'org-adm:INSTITution1');
-- inactiveUser2 -> orga-adm
insert into gum.user_group (user_id,group_id) values ((select id from gum.user where id = 'inactiveUser2'), 'org-adm:institution4');

---
-- Insert user consents
---
INSERT INTO gum.user_consent (id,consent,user_id) VALUES (nextval('gum.user_consent_seq'),true,'user1');
INSERT INTO gum.user_consent (id,consent,user_id) VALUES (nextval('gum.user_consent_seq'),true,'user2');
INSERT INTO gum.user_consent (id,consent,user_id) VALUES (nextval('gum.user_consent_seq'),true,'user3');
INSERT INTO gum.user_consent (id,consent,user_id) VALUES (nextval('gum.user_consent_seq'),true,'inactiveUser');
INSERT INTO gum.user_consent (id,consent,user_id) VALUES (nextval('gum.user_consent_seq'),true,'inactiveUser2');


---
-- Insert organizations
---
INSERT INTO gum.organization (id,shortname,name) VALUES ('1111-1111','myOrga1','myOrganization1');