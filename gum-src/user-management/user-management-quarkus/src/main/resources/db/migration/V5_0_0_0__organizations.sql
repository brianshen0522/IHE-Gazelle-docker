create table gum.organization (
      id varchar(255) not null,
      name varchar(255) unique,
      shortname varchar(32) unique,
      archived boolean not null default false,
      last_update_timestamp timestamp(6) with time zone,
      primary key (id)
);

create table gum.delegated_organization (
    external_id varchar(255),
    idp_id varchar(255),
    organization_id varchar(255) not null,
    primary key (organization_id)
);


alter table if exists gum.delegated_organization
    add constraint fk_delegated_organization_organization
    foreign key (organization_id)
    references gum.organization;
