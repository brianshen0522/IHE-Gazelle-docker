insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (200, true, 'code', 'pierre.caillou@test.email.com', 'Pierre', now(), 'Caillou', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (201, true, 'code', 'jean.bonbeur@test.email.com', 'Jean', now(), 'Bonbeur', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (202, true, 'code', 'pierre.rocher@test.email.com', 'Pierre', now(), 'Rocher', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (203, true, 'code', 'alex.presso@test.email.com', 'Alex', now(), 'Presso', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (204, true, 'code', 'moka.chino@test.email.com', 'Moka', now(), 'Chino', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (205, true, 'code', 'kapu.chino@test.email.com', 'Kapu', now(), 'Chino', now(), 12, 'orgaId', now());

insert into gum.user_preference (languages_spoken, notified_by_email, profile_picture, profile_thumbnail, table_label,
                                 user_id)
values ('fr', true, pg_read_binary_file('/var/log/images/img.jpg'),
        pg_read_binary_file('/var/log/images/thumbnail.jpg'),
        'table', '202');

insert into gum.user_preference (languages_spoken, notified_by_email, profile_picture, profile_thumbnail, table_label,
                                 user_id)
values ('fr', true, pg_read_binary_file('/var/log/images/img.jpg'),
        pg_read_binary_file('/var/log/images/thumbnail.jpg'),
        'table', '201');

insert into gum.user_preference (languages_spoken, notified_by_email, profile_picture, profile_thumbnail, table_label,
                                 user_id)
values ('fr', true, pg_read_binary_file('/var/log/images/img_rotated.jpg'),
        pg_read_binary_file('/var/log/images/thumbnail_rotated.jpg'),
        'table', '205');
