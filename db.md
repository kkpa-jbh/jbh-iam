# Database Migration

## Create DB

```
CREATE DATABASE "jbh_iam_users" WITH OWNER = jbh_admin ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8' TABLESPACE = pg_default CONNECTION LIMIT = -1;
```

## Create extension

```
CREATE EXTENSION pgcrypto;
```

## Set global variables

```
ALTER DATABASE "jbh_iam_users" SET TIME ZONE 'America/Bogota';
```

## Remove All Data

```
-- Remove All Data
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM sub_categories;
DELETE FROM categories;
DELETE FROM users_group;
DELETE FROM user_roles;
DELETE FROM jbh_user;
```