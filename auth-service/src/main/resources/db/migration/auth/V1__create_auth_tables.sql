-- Verification tokens table
CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP
);

CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);

-- Auth tokens table
CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    refresh_token TEXT NOT NULL,
    is_blacklisted BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_auth_tokens_refresh_token UNIQUE (refresh_token)
);

-- Add index for faster token lookups
CREATE INDEX idx_auth_tokens_refresh_token ON auth_tokens(refresh_token);

-- Add index for user_id lookups
CREATE INDEX idx_auth_tokens_user_id ON auth_tokens(user_id);

-- Add index for expiration cleanup
CREATE INDEX idx_auth_tokens_expires_at ON auth_tokens(expires_at);
