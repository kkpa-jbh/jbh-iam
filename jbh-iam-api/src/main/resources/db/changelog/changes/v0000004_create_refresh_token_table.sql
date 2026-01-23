-- Create refresh_token table for JWT refresh token mechanism
-- Author: JBH IAM Team
-- Date: 2025-12-15

CREATE TABLE iam.refresh_token (
    id UUID DEFAULT gen_random_uuid() NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES iam.user(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_refresh_token_user_id ON iam.refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires_at ON iam.refresh_token(expires_at);
CREATE INDEX idx_refresh_token_hash ON iam.refresh_token(token_hash);

-- Comments for documentation
COMMENT ON TABLE iam.refresh_token IS 'Stores refresh tokens for JWT authentication. Tokens are hashed for security.';
COMMENT ON COLUMN iam.refresh_token.token_hash IS 'SHA-256 hash of the refresh token. Never store plain tokens.';
COMMENT ON COLUMN iam.refresh_token.revoked IS 'True if token has been revoked (logout or security incident)';
COMMENT ON COLUMN iam.refresh_token.device_info IS 'User-Agent from the device that requested the token';
COMMENT ON COLUMN iam.refresh_token.ip_address IS 'IP address of the device that requested the token';
