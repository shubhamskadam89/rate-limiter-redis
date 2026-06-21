CREATE TABLE users(
                      id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                      uuid VARCHAR(36) NOT NULL unique,
                      email VARCHAR(255) NOT NULL UNIQUE,
                      password_hash VARCHAR(255) NOT NULL,
                      role ENUM('USER','VIP','ADMIN') NOT NULL DEFAULT 'USER',
                      is_active BOOLEAN NOT NULL DEFAULT true,
                      created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                      updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) on update CURRENT_TIMESTAMP(3),
                      deleted_at DATETIME(3) null,
                      INDEX idx_email (email),
                      INDEX idx_uuid (uuid),
                      INDEX idx_deleted_at(deleted_at)
);

CREATE TABLE products(
                         id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         uuid VARCHAR(36) NOT NULL unique,
                         name VARCHAR(255) not null,
                         description TEXT,
                         base_price DECIMAL(12,2) NOT NULL,
                         metadata JSON,
                         is_active BOOLEAN NOT NULL DEFAULT true,
                         created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                         updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) on update CURRENT_TIMESTAMP(3),
                         deleted_at DATETIME(3) null,
                         INDEX idx_uuid (uuid),
                         INDEX idx_name (name),
                         INDEX idx_deleted_at (deleted_at),
                         INDEX idx_is_active (is_active)

);