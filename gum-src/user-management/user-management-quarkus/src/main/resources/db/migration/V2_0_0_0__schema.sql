CREATE SCHEMA IF NOT EXISTS gum;
create sequence gum.role_seq start with 1 increment by 50;
create sequence gum.user_consent_seq start with 1 increment by 50;

create table gum.user_consent (
                              id integer not null,
                              user_id varchar(255) unique,
                              consent boolean,
                              primary key (id)
);

create table gum.user_consent_history (
                              consent_id integer not null,
                              timestamp timestamp(6),
                              decision varchar(255),
                              primary key (consent_id,timestamp)
);

create table gum.credentials (
                             credentials varchar(255),
                             reset_password boolean,
                             user_id varchar(255) not null,
                             primary key (user_id)
);

create table gum.role (
                       id integer not null,
                       name varchar(255) unique,
                       description varchar(255),
                       primary key (id)
);

create table gum.user (
                       id varchar(255) not null,
                       activated boolean not null,
                       activation_code varchar(255),
                       login_counter integer,
                       email varchar(255) unique not null,
                       firstname varchar(255),
                       organization_id varchar(255),
                       last_login_timestamp timestamp(6),
                       lastname varchar(255),
                       last_update_timestamp timestamp(6),
                       registration_timestamp timestamp(6),
                       primary key (id)
);

create table gum.user_role (
                             user_id varchar(255) not null,
                             role_id integer not null,
                             primary key (user_id, role_id)
);

alter table if exists gum.credentials
    add constraint fk_credentials_user
    foreign key (user_id)
    references gum.user ON DELETE CASCADE;

alter table if exists gum.user_role
    add constraint fk_role_user
    foreign key (role_id)
    references gum.role;

alter table if exists gum.user_role
    add constraint fk_user_role
    foreign key (user_id)
    references gum.user ON DELETE CASCADE;

alter table if exists gum.user_consent_history
    add constraint fk_user_consent
    foreign key (consent_id)
    references gum.user_consent;