
---
-- Insert organization
---
-- Kereval
INSERT INTO usr_institution (id,keyword,name,url,institution_type_id) values (1,'KER','KEREVAL','https://www.kereval.com',2);
-- INSTITution1
INSERT INTO usr_institution (id,keyword,name,note,url,institution_type_id, last_changed) values (2,'INSTITution1','INSTITution1 name','description','https://my.institution1.com',2, '2024-01-05 14:01:10.00');
-- institution2
INSERT INTO usr_institution (id,keyword,name,note,url,institution_type_id, last_changed) values (3,'institution2','institution2 name','description','https://www.kereval.com',2, '2024-01-05 14:02:10.00');
-- institution3
INSERT INTO usr_institution (id,keyword,name,note,url,institution_type_id) values (4,'institution3','institution3 name','description','https://www.kereval.com',2);
-- institution4
INSERT INTO usr_institution (id,keyword,name,note,url,institution_type_id) values (5,'institution4','institution4 name','description','https://www.kereval.com',2);
-- delegatedInstitution5
INSERT INTO usr_institution (id,keyword,name,note,url,institution_type_id) values (6,'delegInsti5','delegated Institution5','description','https://deleg.institution5.com',2);
INSERT INTO usr_delegated_organization (organization_id,external_id,idp_id) values (6,'insti5-external-id','insti5-idp-id');
-- orgaNotMigrated
INSERT INTO usr_institution (id,keyword,name,note,url,institution_type_id) values (7,'orgaNotMigrated','orgaNotMigrated name','description','https://not_migrated.com',2);

---
-- Insert users
---
-- migratedUser
insert into usr_users (id,email,password,username,firstname,lastname,institution_id,activated,creation_date, last_login,last_changed,counter_logins,activation_code) values
    (nextval('usr_users_id_seq'),'migrated-user@gazelle.com',MD5('aZeRtY'),'migrated-user','migratedUser fn','migratedUser ln',
     1,true,'2024-01-05 14:01:10.00', '2024-02-05 14:01:10.00','2024-03-05 14:01:10.00',0,'uniqueActivationCode');
insert into usr_users (id,email,password,username,firstname,lastname,institution_id,activated,creation_date, last_login,last_changed,counter_logins,activation_code) values
    (nextval('usr_users_id_seq'),'user@gazelle.com',MD5('aZeRtY'),'user','User fn','User ln',
     2,true,'2024-01-05 14:01:10.00', '2024-02-05 14:01:10.00','2024-03-05 14:01:10.00',1,'');
insert into usr_users (id,email,password,username,firstname,lastname,institution_id,activated,creation_date, last_login,last_changed,counter_logins,activation_code) values
    (nextval('usr_users_id_seq'),'conflictUser@Gazelle.com',MD5('aZeRtY'),'conflictUser','conflictUser fn','conflictUser ln',
     6,false,'2024-01-05 14:01:10.00', '2024-02-05 14:01:10.00','2024-03-05 14:01:10.00',0,'uniqueActivationCode');
insert into usr_users (id,email,password,username,firstname,lastname,institution_id,activated,creation_date, last_login,last_changed,counter_logins,activation_code) values
    (nextval('usr_users_id_seq'),'conflictuser@gazelle.com',MD5('aZeRtY'),'conflictUser2','conflictUser2 fn','conflictUser2 ln',
     6,true,'2024-01-05 14:01:10.00', '2024-02-05 14:01:10.00','2024-03-05 14:01:10.00',7,'uniqueActivationCode');
---
-- Insert roles
---
-- admin_role
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (nextval('usr_role_id_seq'), NULL, NULL, 'Profile with Admin rights (ie. a user allowed to do everything)', 'admin_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (nextval('usr_role_id_seq'), NULL, NULL, '', 'monitor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (nextval('usr_role_id_seq'), NULL, NULL, '', 'vendor_admin_role');

---
-- Insert user roles
---
-- migratedUser -> admin_role
insert into usr_user_role (user_id,role_id) values
    ((select id from usr_users where username = 'migrated-user'), (select id from usr_role where name ='admin_role'));
-- migratedUser -> monitor_role
insert into usr_user_role (user_id,role_id) values
    ((select id from usr_users where username = 'migrated-user'), (select id from usr_role where name ='monitor_role'));
-- migratedUser -> vendor_admin_role
insert into usr_user_role (user_id,role_id) values
    ((select id from usr_users where username = 'migrated-user'), (select id from usr_role where name ='vendor_admin_role'));

---
-- Insert user photo
---
insert into tm_user_photo (id,photo_bytes) values (nextval('tm_user_preferences_id_seq'),pg_read_binary_file('/common-resources/gum/postgres/img/sample.jpeg'));

---
-- Insert user preferences
---
insert into tm_user_preferences (id,userphoto_id,email_notification,username,spoken_languages,connectathon_table) values
    (nextval('tm_user_preferences_id_seq'),1,true,'migrated-user','fr,de','C4');
insert into tm_user_preferences (id,userphoto_id,email_notification,username,spoken_languages,connectathon_table) values
    (nextval('tm_user_preferences_id_seq'),null,false,'conflictuser2','','');