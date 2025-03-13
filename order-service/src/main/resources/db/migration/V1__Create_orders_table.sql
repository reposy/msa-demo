CREATE TABLE IF NOT EXISTS orders
(
    id         SERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    product_id BIGINT      NOT NULL,
    quantity   INT         NOT NULL,
    status     VARCHAR(50) NOT NULL
    );