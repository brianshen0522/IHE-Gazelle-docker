create table gum.group (
                           id varchar(255) not null,
                           name varchar(255),
                           reference varchar(255),
                           type varchar(255) check (type in ('ORGANIZATION','ORGANIZATION_ADMIN','ROLE')),
                           primary key (id)
);

create table gum.user_group (
                           user_id varchar(255) not null,
                           group_id varchar(255) not null,
                           primary key (user_id, group_id)
);

create table gum.group_ingroup (
                           group_id varchar(255) not null,
                           in_group_id varchar(255)
);

alter table if exists gum.group_ingroup
    add constraint fk_group_ingroup
        foreign key (group_id)
            references gum.group;


alter table if exists gum.user_group
    add constraint fk_group_user
    foreign key (group_id)
    references gum.group;

alter table if exists gum.user_group
    add constraint fk_user_group
    foreign key (user_id)
    references gum.user
    on delete cascade;

-- Init default group roles
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:gazelle_admin','ROLE','gazelle_admin','Gazelle super administrator') ON CONFLICT DO NOTHING ;
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:monitor','ROLE','monitor','Gazelle testing session monitor') ON CONFLICT DO NOTHING ;
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:testing_session_manager','ROLE','testing_session_manager','Gazelle testing session manager') ON CONFLICT DO NOTHING ;
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:test_designer','ROLE','test_designer','Gazelle test designer') ON CONFLICT DO NOTHING ;
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:sut_operator','ROLE','sut_operator','Gazelle system under test operator') ON CONFLICT DO NOTHING ;
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:late_registration','ROLE','late_registration','Authorized to register lately') ON CONFLICT DO NOTHING ;
INSERT INTO gum.group (id, type, reference, name) VALUES ('role:project_admin','ROLE','project_admin','Gazelle project administrator') ON CONFLICT DO NOTHING ;