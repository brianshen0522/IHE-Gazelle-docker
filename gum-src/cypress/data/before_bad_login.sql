-- cy_auth_blocked
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter) values
    ('cy_auth_blocked','cy_auth_blocked@gazelle.com','cy_auth_blocked fn','cy_auth_blocked ln',false,'2020-10-05 14:01:10.00','cy_auth_orga','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',10);
insert into gum.credentials (user_id,credentials,reset_password) values ('cy_auth_blocked', CONCAT('{"password":"',MD5('password'),'", "hashMethod":"MD5"}'),false);
insert into gum.user_role (user_id,role_id) values
    ((select id from gum.user where id = 'cy_auth_blocked'), (select id from gum.role where name ='admin_role'));

-- cy_auth_inactive_vendor_admin
insert into gum.user (id,email,firstname,lastname,activated,last_login_timestamp,organization_id,last_update_timestamp,registration_timestamp,login_counter,activation_code) values
    ('cy_auth_inactive_vendor_admin','cy_auth_inactive_vendor_admin@gazelle.com','cy_auth_inactive_vendor_admin fn','cy_auth_inactive_vendor_admin ln',false,'2020-10-05 14:01:10.00','cy_auth_orga','2020-10-05 14:01:10.00','1980-10-05 14:01:10.00',0,'activation_code');
insert into gum.credentials (user_id,credentials,reset_password) values ('cy_auth_inactive_vendor_admin', CONCAT('{"password":"',MD5('password'),'", "hashMethod":"MD5"}'),false);
insert into gum.user_role (user_id,role_id) values
    ((select id from gum.user where id = 'cy_auth_inactive_vendor_admin'), (select id from gum.role where name ='vendor_admin_role'));