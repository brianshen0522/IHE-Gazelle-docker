create table gum.delegated_user (
        external_id varchar(255),
        idp_id varchar(255),
        user_id varchar(255) not null,
        primary key (user_id)
);

alter table if exists gum.delegated_user
    add constraint FK1n0aw2s9ebbyp18mco1hr083
    foreign key (user_id)
    references gum.user;