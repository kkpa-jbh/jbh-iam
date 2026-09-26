-- Add Google identity to iam.user for Google sign-in
-- Author: JBH IAM Team
-- Date: 2026-09-26

ALTER TABLE iam.user ADD COLUMN google_sub VARCHAR(255);

-- One Google account can link to only one user. NULL is allowed many times.
ALTER TABLE iam.user ADD CONSTRAINT UK_user_google_sub UNIQUE (google_sub);

COMMENT ON COLUMN iam.user.google_sub IS 'Google account id (the "sub" claim of the ID token). It never changes, unlike the email. NULL for users who never signed in with Google.';
