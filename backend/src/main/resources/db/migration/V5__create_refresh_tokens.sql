CREATE TABLE refresh_tokens(
                               id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                               uuid VARCHAR(36) NOT NULL UNIQUE,

                               token VARCHAR(255) NOT NULL UNIQUE,

                               user_id BIGINT UNSIGNED NOT NULL,

                               expires_at DATETIME(3) NOT NULL,

                               is_revoked BOOLEAN NOT NULL DEFAULT FALSE,

                               created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                   ON UPDATE CURRENT_TIMESTAMP(3),

                               deleted_at DATETIME(3) NULL,

                               CONSTRAINT fk_refresh_token_user
                                   FOREIGN KEY(user_id)
                                       REFERENCES users(id),

                               INDEX idx_refresh_token(token),
                               INDEX idx_refresh_user(user_id),
                               INDEX idx_deleted_at(deleted_at)
);