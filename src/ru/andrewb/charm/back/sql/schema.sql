drop table if exists profile_like;
drop table if exists profile cascade;

create table profile (
    id              bigserial primary key,
    email           varchar not null unique,
    password        varchar not null,
    "name"          varchar,
    surname         varchar,
    birthdate       date,
    about           text,
    gender          varchar,
    photo           varchar,
    status          varchar not null default 'INACTIVE',
    role            varchar not null default 'USER',
    created_date    timestamp not null default current_timestamp
);

create table profile_like (
    from_profile    bigint not null references profile (id),
    to_profile      bigint not null references profile (id),
    is_like         boolean not null,
    is_match        boolean not null,
    created_date    timestamp not null default current_timestamp,
    primary key (from_profile, to_profile)
);