create table gum.user_preference (
                                     languages_spoken varchar(255),
                                     notified_by_email boolean,
                                     profile_picture bytea,
                                     profile_thumbnail bytea,
                                     table_label varchar(255),
                                     user_id varchar(255) not null,
                                     primary key (user_id)
);

alter table if exists gum.user_preference
    add constraint  fk_user_preference_user
        foreign key (user_id)
            references gum.user
            on delete cascade;
