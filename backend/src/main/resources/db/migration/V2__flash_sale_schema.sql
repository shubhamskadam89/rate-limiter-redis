CREATE TABLE sale_events(
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    product_id BIGINT UNSIGNED NOT NULL,
    sale_price DECIMAL(12,2) NOT NULL,
    total_inventory INT UNSIGNED NOT NULL,
    final_count INT UNSIGNED NULL,
    max_per_user TINYINT UNSIGNED NOT NULL DEFAULT 1,
    start_time DATETIME(3) NOT NULL,
    end_time DATETIME(3) NOT NULL,
    status ENUM('DRAFT','ACTIVE','ENDED',
        'CANCELLED') NOT NULL  DEFAULT 'DRAFT',
    created_by BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(3)
                        ON UPDATE CURRENT_TIMESTAMP(3),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_status_start (status,start_time),
    INDEX idx_uuid (uuid),
    INDEX idx_current_stock (total_inventory,final_count)
);

CREATE TABLE orders(
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT UNSIGNED NOT NULL,
    sale_event_id BIGINT UNSIGNED,
    product_id BIGINT UNSIGNED NOT NULL,
    quantity  TINYINT UNSIGNED NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING','CONFIRMED','CANCELLED')
        NOT NULL DEFAULT 'CONFIRMED',
    idempotency_key VARCHAR(36) NOT NULL UNIQUE,
    created_at DATETIME(3) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE current_timestamp(3),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (sale_event_id) REFERENCES sale_events(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_user_sale (user_id, sale_event_id),
    INDEX idx_sale_event (sale_event_id),
    INDEX idx_idempotency (idempotency_key),
    INDEX idx_created_at (created_at)

);