CREATE TABLE rate_limit_configs (

                                    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

                                    endpoint_pattern VARCHAR(255) NOT NULL,

                                    algorithm ENUM(
                                        'FIXED_WINDOW',
                                        'SLIDING_WINDOW',
                                        'TOKEN_BUCKET'
                                        ) NOT NULL,

                                    requests_limit INT UNSIGNED NOT NULL,

                                    window_seconds INT UNSIGNED NOT NULL,

                                    burst_capacity INT UNSIGNED NULL,

                                    refill_rate DECIMAL(8,2) NULL,

                                    applies_to_role ENUM(
                                        'USER',
                                        'VIP',
                                        'ADMIN',
                                        'IP'
                                        ) NOT NULL DEFAULT 'USER',

                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,

                                    created_at DATETIME(3) NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP(3),

                                    updated_at DATETIME(3) NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP(3)
                                        ON UPDATE CURRENT_TIMESTAMP(3),

                                    UNIQUE KEY uk_endpoint_role (
                                                                 endpoint_pattern,
                                                                 applies_to_role
                                        ),

                                    INDEX idx_active (is_active),

                                    INDEX idx_endpoint (endpoint_pattern),

                                    INDEX idx_algorithm (algorithm)

);