insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (150, true, 'code', 'test.email@test.email.com', 'Jean', now(), 'Bonbeur', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (151, true, 'code', 'test.email2@test.email.com', 'Jean', now(), 'Bonbeur', now(), 12, 'orgaId', now());

insert into gum.user(id, activated, activation_code, email, firstname, last_login_timestamp, lastname,
                     last_update_timestamp, login_counter, organization_id, registration_timestamp)
values (152, true, 'code', 'test.email3@test.email.com', 'Jean', now(), 'Bonbeur', now(), 12, 'orgaId', now());

insert into gum.user_preference (languages_spoken, notified_by_email, profile_picture, profile_thumbnail, table_label,
                                 user_id)
values ('fr', true, pg_read_binary_file('/var/log/images/img.jpg'),
        pg_read_binary_file('/var/log/images/thumbnail.jpg'),
        'table', '150');

insert into gum.user_preference (languages_spoken, notified_by_email, profile_picture, profile_thumbnail, table_label,
                                 user_id)
values ('fr', true, pg_read_binary_file('/var/log/images/img.jpg'),
        pg_read_binary_file('/var/log/images/thumbnail.jpg'),
        'table', '151');


