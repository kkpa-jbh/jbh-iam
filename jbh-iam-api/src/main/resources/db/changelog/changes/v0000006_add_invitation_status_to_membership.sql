-- Add invitation status columns to user_group_membership table
-- Author: JBH IAM Team
-- Date: 2025-01-26

ALTER TABLE iam.user_group_membership ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED';
ALTER TABLE iam.user_group_membership ADD COLUMN invited_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE iam.user_group_membership ADD COLUMN accepted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE iam.user_group_membership ADD COLUMN declined_at TIMESTAMP WITH TIME ZONE;

-- Update existing records: set invited_at and accepted_at to added_at for existing members
UPDATE iam.user_group_membership
SET invited_at = added_at,
    accepted_at = added_at
WHERE status = 'ACCEPTED';

-- Create index for status queries
CREATE INDEX idx_user_group_membership_status ON iam.user_group_membership(status);

-- Comments for documentation
COMMENT ON COLUMN iam.user_group_membership.status IS 'Invitation status: PENDING_TO_ACCEPT, ACCEPTED, DECLINED';
COMMENT ON COLUMN iam.user_group_membership.invited_at IS 'Timestamp when the invitation was created';
COMMENT ON COLUMN iam.user_group_membership.accepted_at IS 'Timestamp when the invitation was accepted';
COMMENT ON COLUMN iam.user_group_membership.declined_at IS 'Timestamp when the invitation was declined';
