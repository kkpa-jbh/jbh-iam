-- Add created_by column to user_group table
-- Author: JBH IAM Team
-- Date: 2025-01-26

ALTER TABLE iam.user_group ADD COLUMN created_by UUID;

ALTER TABLE iam.user_group
ADD CONSTRAINT fk_user_group_created_by
    FOREIGN KEY (created_by) REFERENCES iam.user(id) ON DELETE SET NULL;

CREATE INDEX idx_user_group_created_by ON iam.user_group(created_by);

COMMENT ON COLUMN iam.user_group.created_by IS 'UUID of the user who created this user group';
