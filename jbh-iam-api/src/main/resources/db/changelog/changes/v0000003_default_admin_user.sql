-- Insert default admin user with NULL password (to be handled by application startup)
INSERT INTO iam.user(user_name, email, nick_name, password, created_at)
VALUES ('System Administrator', 'sysadmin@jbh.com', 'admin', NULL, CURRENT_TIMESTAMP);

-- Assign ADMIN role to the default admin user
INSERT INTO iam.user_roles(user_id, role_id)
SELECT u.id, r.id 
FROM iam.user u, iam.roles r 
WHERE u.email = 'sysadmin@jbh.com' AND r.name = 'ROLE_ADMIN';