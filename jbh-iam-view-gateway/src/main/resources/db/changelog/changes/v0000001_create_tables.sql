create table jbh_user (
                        id UUID default gen_random_uuid() not null,
                        user_name varchar(255),
                        email varchar(255),
                        nick_name varchar(255),
                        password varchar(255),
                        created_at timestamp with time zone  not null,
                        updated_at timestamp with time zone ,
                        primary key (id)
);
COMMENT ON COLUMN jbh_user.user_name IS 'User userName, It is composd by first Name and Last Name';

create table users_group (
                           id UUID default gen_random_uuid() not null,
                           name varchar(255),
                           single boolean,
                           user_id UUID,
                           created_at timestamp with time zone  not null,
                           updated_at timestamp with time zone ,
                           primary key (id)
);


create table categories_default (
                                  id integer not null,
                                  name varchar(50) not null,
                                  created_at timestamp with time zone  not null,
                                  updated_at timestamp with time zone ,
                                  primary key (id)
);

create table sub_categories_default (
                                      id integer not null,
                                      category_default_id integer ,
                                      name varchar (50) not null,
                                      created_at timestamp with time zone  not null,
                                      updated_at timestamp with time zone ,
                                      primary key (id)
);

create table categories (
                          id UUID default gen_random_uuid() not null,
                          name varchar(50) not null,
                          category_default_id integer not null,
                          users_group_id UUID not null,
                          created_at timestamp with time zone  not null,
                          updated_at timestamp with time zone ,
                          primary key (id)
);

COMMENT ON TABLE categories IS 'Categories defined for all users';
COMMENT ON COLUMN categories.name IS 'category userName, can be the same for a different user';

create table sub_categories (
                              id UUID default gen_random_uuid() not null,
                              name varchar(255),
                              category_id UUID,
                              sub_category_default_id integer not null,
                              created_at timestamp with time zone  not null,
                              updated_at timestamp with time zone ,
                              primary key (id)
);


create table roles (
                     id UUID default gen_random_uuid() not null,
                     name varchar(60),
                     primary key (id)
);

create table accounts (
                        id UUID default gen_random_uuid() not null,
                        user_group_id UUID,
                        description varchar(255),
                        active_balance numeric(19, 2),
                        passive_balance numeric(19, 2),
                        created_at timestamp with time zone  not null,
                        updated_at timestamp with time zone ,
                        primary key (id)
);



create table user_roles (
                          user_id UUID not null,
                          role_id UUID not null,
                          primary key (user_id, role_id)
);

create table transactions (
                              id UUID default gen_random_uuid() not null,
                              sub_category_id UUID not null,
                              effective_date date not null,
                              account_id UUID not null,
                              description varchar(255),
                              total_value numeric(19, 2),
                              created_at timestamp with time zone  not null,
                              updated_at timestamp with time zone ,
                              primary key (id)
);

alter table if exists jbh_user
  add constraint UK_jbh_user_name unique (email);


alter table if exists roles
  add constraint UK_roles_name unique (name);


alter table if exists users_group_categories
  add constraint UK_users_group_category_id unique (category_id);


alter table if exists accounts
  add constraint FK_accounts_by_user_group_id
    foreign key (user_group_id)
      references users_group;


alter table if exists categories
  add constraint FK_categories_by_categories_default
    foreign key (category_default_id)
      references categories_default;

alter table if exists categories
  add constraint FK_categories_by_users_group
    foreign key (users_group_id)
      references users_group;

alter table if exists sub_categories
  add constraint FK_sub_categories_by_category
    foreign key (category_id)
      references categories;

alter table if exists sub_categories
  add constraint FK_sub_categories_by_sub_category_default
    foreign key (sub_category_default_id)
      references sub_categories_default;

alter table if exists sub_categories_default
  add constraint FK_sub_categories_default_by_category_default
    foreign key(category_default_id)
      references categories_default;

create unique index sub_categories_default_id_category_default_id_uindex
  on sub_categories_default (id, category_default_id);

alter table if exists user_roles
  add constraint FK_user_roles_by_role_id
    foreign key (role_id)
      references roles;


alter table if exists user_roles
  add constraint FK_user_roles_by_user_id
    foreign key (user_id)
      references jbh_user;


alter table if exists users_group
  add constraint FK_users_group_by_user_id
    foreign key (user_id)
      references jbh_user;

ALTER TABLE IF EXISTS transactions
  add constraint FK_transactions_by_account_id
    foreign key (account_id)
      references accounts;

ALTER TABLE IF EXISTS transactions
  add constraint FK_transactions_by_sub_category_id
    foreign key (sub_category_id)
      references sub_categories;