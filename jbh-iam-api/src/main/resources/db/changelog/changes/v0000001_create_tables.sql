CREATE SCHEMA iam;

create table iam.user
(
    id         UUID default gen_random_uuid() not null,
    user_name  varchar(100),
    email      varchar(255),
    nick_name  varchar(50),
    password   varchar(255),
    created_at timestamp with time zone       not null,
    updated_at timestamp with time zone,
    primary key (id)
);
COMMENT ON COLUMN iam.user.user_name IS 'User userName, It is composed by first Name and Last Name';

create table iam.user_group
(
    id         UUID default gen_random_uuid() not null,
    short_name varchar(20)                    not null unique,
    name       varchar(50),
    created_at timestamp with time zone       not null,
    updated_at timestamp with time zone,
    primary key (id)
);

CREATE TABLE iam.user_group_membership
(
    group_id UUID REFERENCES iam.user_group (id) ON DELETE CASCADE,
    user_id  UUID REFERENCES iam.user (id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id)
);

create table iam.roles
(
    id   UUID default gen_random_uuid() not null,
    name varchar(50),
    primary key (id)
);

create table iam.user_roles
(
    user_id UUID not null,
    role_id UUID not null,
    primary key (user_id, role_id)
);

alter table if exists iam.user
    add constraint UK_jbh_user_name unique (email);


alter table if exists iam.roles
    add constraint UK_roles_name unique (name);

alter table if exists iam.user_roles
    add constraint FK_user_roles_by_role_id
        foreign key (role_id)
            references iam.roles;

alter table if exists iam.user_roles
    add constraint FK_user_roles_by_user_id
        foreign key (user_id)
            references iam.user;

-- Index for finding groups an user belongs to
CREATE INDEX idx_user_group_membership_user_id ON iam.user_group_membership (user_id);

-- Index for finding members of a specific group
CREATE INDEX idx_user_group_membership_group_id ON iam.user_group_membership (group_id);